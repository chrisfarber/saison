(ns saison.nic
  (:import [java.net NetworkInterface]))

(defn- network-interfaces []
  (enumeration-seq (NetworkInterface/getNetworkInterfaces)))

(defn- nic-info [nic]
  {:display-name (.getDisplayName nic)
   :inet-addrs (map (fn [addr]
                      {:addr (.getHostAddress addr)
                       :site-local (.isSiteLocalAddress addr)
                       :link-local (.isLinkLocalAddress addr)
                       :loopback (.isLoopbackAddress addr)})
                    (enumeration-seq (.getInetAddresses nic)))})

(defn- only-addrs [a-nic-info]
  (map (fn [info]
         (assoc info
                :display-name (:display-name a-nic-info)))
       (:inet-addrs a-nic-info)))

(defn- candidate? [addr-info]
  (let [{:keys [site-local link-local loopback]} addr-info]
    (and site-local
         (not link-local)
         (not loopback))))

(defn addr-infos []
  (mapcat (comp only-addrs nic-info) (network-interfaces)))

(defn guess-local-addr
  "A heuristic for a local IP address of the machine. It's
   probably quite broken, but seems to work on my mac.
   
   Returns a map with keys :addr, :display-name, :site-local,
   :link-local, and :loopback"
  []
  ;; sort by display name in attempt to get the preferred nic.
  ;; e.g., on my imac pro, en0 is ethernet and en1 is wifi.
  (first (sort-by :display-name (filter candidate? (addr-infos)))))

(defn guess-local-ip
  "A heuristic for a local IP address for the machine. It's 
   probably quite broken, but seems to work on my macs.
   
   Returns a string."
  []
  (:addr (guess-local-addr)))