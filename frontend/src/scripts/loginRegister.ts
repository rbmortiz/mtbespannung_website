import { getEl, manageNavBarLinks, initRacketBackground } from "./HelperFunctions";

const emailInput = getEl<HTMLInputElement>("emailInput");
const passwordInput = getEl<HTMLInputElement>("passwordInput");
const firstNameInput = getEl<HTMLInputElement>("firstNameInput");
const lastNameInput = getEl<HTMLInputElement>("lastNameInput");

const loginButton = getEl<HTMLButtonElement>("loginButton");
const registerPullupButton = getEl<HTMLButtonElement>("registerPullupButton");
const registerSendButton = getEl<HTMLButtonElement>("registerSendButton");

const accountText = getEl<HTMLParagraphElement>("accountText");


if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

function init(){

    //checks
    manageNavBarLinks();
    redirectIfLoggedIn();

    //background init
    initRacketBackground();

    // Button Events
    registerPullupButton.addEventListener("click", () => {
        void registerPullupPress();
    });

    loginButton.addEventListener("click", () => {
        void sendLogin();
    });

    registerSendButton.addEventListener("click", () => {
        void sendRegister();
    });
}

function redirectIfLoggedIn(): void {
    let token = localStorage.getItem("token");
    if(token){
        window.location.replace("/src/pages/main.html");
    }
}

function displayElement(element: HTMLElement, display: boolean){
    if(!display){
        if(element.classList.contains("d-none")) return;
        else element.classList.add("d-none");
    } else {
        if(!element.classList.contains("d-none")) return;
        else element.classList.remove("d-none");
    }
}

function showError(message: string): void {
  const errorBox = getEl<HTMLDivElement>("loginError");
  errorBox.textContent = message;
  errorBox.classList.remove("d-none");
}

// Button presses

function registerPullupPress(): void {
    displayElement(registerPullupButton, false);
    displayElement(accountText, false);
    displayElement(loginButton, false);
    displayElement(firstNameInput, true);
    displayElement(lastNameInput, true);
    displayElement(registerSendButton, true);

    firstNameInput.value = "";
    lastNameInput.value = "";

    firstNameInput.required = true;
    lastNameInput.required = true;
}

// fetch functions

async function sendRegister(): Promise<void> {
    const email = emailInput.value;
    const password = passwordInput.value;
    const firstName = firstNameInput.value;
    const lastName = lastNameInput.value;

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/register",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    password: password,
                    firstname: firstName,
                    lastname: lastName
                })
            }
        );

        if (response.ok) {
            console.log("Register successful!");

            // Send user to login page
            window.location.replace("/src/pages/loginRegister.html");
        } else {
            const error = await response.text();

            console.log("Register failed:", error);
            showError(error);
        }

    } catch (error) {
        console.error("Could not reach backend:", error);
        showError("Server konnte nicht erreicht werden");
    }
}

async function sendLogin(): Promise<void> {
    const email = emailInput.value;
    const password = passwordInput.value;

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/loginUser",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    email: email,
                    password: password
                })
            }
        );

        if (response.ok) {
            const data = await response.json();

            console.log("Login successful!");

            localStorage.setItem("token", data.token);

            window.location.replace("/src/pages/main.html");
        } else {
            console.log("Login failed!");

            showError("Login fehlgeschlagen");
        }

    } catch (error) {
        console.error("Backend could not be reached:", error);

        showError("Server konnte nicht erreicht werden");
    }
}