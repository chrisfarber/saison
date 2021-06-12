function waitForReload() {
  fetch("/__reload").then(
    (r) => {
      window.location.reload(true);
    },
    (err) => {
      console.log("err?", err);
      setTimeout(waitForReload, 200);
    },
  );
}
waitForReload();
