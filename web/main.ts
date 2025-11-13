setInterval(() => {
  const fps = (12 + Math.random() * 8).toFixed(1);
  (document.getElementById("fps") as HTMLElement).textContent = fps;
}, 1000);