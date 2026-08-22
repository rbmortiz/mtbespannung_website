export function getEl<T extends HTMLElement>(id: string): T {
    const el = document.getElementById(id);

    if (!el) {
        throw new Error(`Expected element #${id} to exist in login.html`);
    }

    return el as T;
}

export function manageNavBarLinks(): void {

    let loginLink = getEl<HTMLLIElement>("loginLink");
    let dashboardLink = getEl<HTMLLIElement>("dashboardLink");
    let bespannungenLink = getEl<HTMLLIElement>("bespannungenLink");
    let trainerstundenLink = getEl<HTMLLIElement>("trainerstundenLink");
    let mainLink = getEl<HTMLLIElement>("mainLink");

    let path = window.location.pathname;
    let token = localStorage.getItem("token");

    if(token){
        loginLink.classList.add("d-none");
        console.log("User is logged in")
    }
    else {
        dashboardLink.classList.add("d-none");
        console.log("User is not logged in");
    }
    
    if(path.includes("loginRegister.html")){
        dashboardLink.classList.add("d-none");
        trainerstundenLink.classList.add("d-none");
        bespannungenLink.classList.add("d-none");
        loginLink.classList.add("d-none");
    }
    else if(path.includes("bespannungen.html")){
        bespannungenLink.classList.add("d-none");
    }
    else if(path.includes("trainerstunden.html")){
        trainerstundenLink.classList.add("d-none");
    }
    else if(path.includes("dashboard.html")){
        dashboardLink.classList.add("d-none");
        loginLink.classList.add("d-none");
    }
    else if(path.includes("main.html")){
        mainLink.classList.add("d-none");
    }
}

export function redirectIfNoToken(): void {
    let token = localStorage.getItem("token");
    if(!token) window.location.replace("/src/pages/main.html");
}

export async function setFirstName(): Promise<void> {
    const userText = getEl<HTMLAnchorElement>("userText");
    let token = localStorage.getItem("token");

    if(!token)return;

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/getUser",
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }
        );

        if (response.ok) {
            const data = await response.json();
            userText.innerHTML = `/${data.firstName}`;
            console.log("username updated");
        } else {
            console.log("Not Valid First Name found");
        }

    } catch (error) {
        console.error("Backend could not be reached:", error);
    }
}

export function initRacketBackground(): void {

  const canvasElement = document.getElementById("star-background");

  if (!(canvasElement instanceof HTMLCanvasElement)) {
    console.error("Canvas #star-background not found");
    return;
  }

  const context = canvasElement.getContext("2d");

  if (!context) {
    console.error("Could not get canvas context");
    return;
  }

  const canvas: HTMLCanvasElement = canvasElement;
  const ctx: CanvasRenderingContext2D = context;

  let mouseX = -1000;
  let mouseY = -1000;

  interface Racket {
    x: number;
    y: number;
    size: number;
    speedX: number;
    speedY: number;
    rotation: number;
    rotationSpeed: number;
  }

  const rackets: Racket[] = [];

  function resizeCanvas(): void {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
  }

  function createRackets(): void {
    rackets.length = 0;

    const amount = Math.floor(
      (canvas.width * canvas.height) / 9000
    );

    for (let i = 0; i < amount; i++) {
      rackets.push({
        x: Math.random() * canvas.width,
        y: Math.random() * canvas.height,

        size: Math.random() * 5 + 7,

        speedX: (Math.random() - 0.5) * 0.4,
        speedY: (Math.random() - 0.5) * 0.4,

        rotation: Math.random() * Math.PI * 2,
        rotationSpeed: (Math.random() - 0.5) * 0.01
      });
    }
  }

  function drawRacket(racket: Racket): void {

    ctx.save();

    ctx.translate(racket.x, racket.y);
    ctx.rotate(racket.rotation);

    ctx.strokeStyle = "rgba(49, 135, 216, 0.65)";
    ctx.lineWidth = 1.2;

    /*
       Racket head
    */

    ctx.beginPath();

    ctx.ellipse(
      0,
      -racket.size * 0.5,
      racket.size * 0.6,
      racket.size,
      0,
      0,
      Math.PI * 2
    );

    ctx.stroke();

    /*
       Handle
    */

    ctx.beginPath();

    ctx.moveTo(0, racket.size * 0.5);
    ctx.lineTo(0, racket.size * 2);

    ctx.stroke();

    /*
       Grip
    */

    ctx.lineWidth = 2;

    ctx.beginPath();

    ctx.moveTo(0, racket.size * 1.4);
    ctx.lineTo(0, racket.size * 2);

    ctx.stroke();

    /*
       Strings
    */

    ctx.lineWidth = 0.4;

    for (let i = -2; i <= 2; i++) {
      const offset = i * racket.size * 0.18;

      ctx.beginPath();

      ctx.moveTo(offset, -racket.size * 1.25);
      ctx.lineTo(offset, racket.size * 0.2);

      ctx.stroke();
    }

    for (let i = -2; i <= 2; i++) {
      const offset = i * racket.size * 0.25;

      ctx.beginPath();

      ctx.moveTo(-racket.size * 0.45, -racket.size * 0.5 + offset);
      ctx.lineTo(racket.size * 0.45, -racket.size * 0.5 + offset);

      ctx.stroke();
    }

    ctx.restore();
  }

  function drawRackets(): void {

    ctx.clearRect(
      0,
      0,
      canvas.width,
      canvas.height
    );

    for (const racket of rackets) {

      // Distance to mouse
      const dx = racket.x - mouseX;
      const dy = racket.y - mouseY;

      const distance = Math.sqrt(
        dx * dx + dy * dy
      );

      // Push racket away from mouse
      if (distance > 0 && distance < 150) {

        const force = (150 - distance) / 70;

        racket.x +=
          (dx / distance) * force * 2;

        racket.y +=
          (dy / distance) * force * 2;

        // Mouse also makes them spin
        racket.rotationSpeed +=
          (Math.random() - 0.5) * 0.002;
      }

      // Movement
      racket.x += racket.speedX;
      racket.y += racket.speedY;

      // Rotation
      racket.rotation += racket.rotationSpeed;

      // Wrap around screen
      if (racket.x < -30) {
        racket.x = canvas.width + 30;
      }

      if (racket.x > canvas.width + 30) {
        racket.x = -30;
      }

      if (racket.y < -30) {
        racket.y = canvas.height + 30;
      }

      if (racket.y > canvas.height + 30) {
        racket.y = -30;
      }

      drawRacket(racket);
    }

    requestAnimationFrame(drawRackets);
  }

  window.addEventListener(
    "mousemove",
    (event) => {
      mouseX = event.clientX;
      mouseY = event.clientY;
    }
  );

  window.addEventListener(
    "mouseleave",
    () => {
      mouseX = -1000;
      mouseY = -1000;
    }
  );

  window.addEventListener(
    "resize",
    () => {
      resizeCanvas();
      createRackets();
    }
  );

  resizeCanvas();
  createRackets();
  drawRackets();
}