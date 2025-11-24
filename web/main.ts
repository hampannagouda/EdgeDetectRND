console.log("Web viewer started");

// Simulated FPS update
let fpsLabel = document.getElementById("fps") as HTMLDivElement;

let fps = 30.0;
setInterval(() => {
  fps = 25 + Math.random() * 10; // fake fps
  fpsLabel.innerText = `FPS: ${fps.toFixed(1)}`;
}, 1000);

// Later, you can load base64 or static images.
