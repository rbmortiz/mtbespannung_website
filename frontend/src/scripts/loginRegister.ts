import { getEl, manageNavBarLinks, initRacketBackground, showError, displayHTMLElement } from "./helpers/HelperFunctions";

const emailInput = getEl<HTMLInputElement>("emailInput");
const passwordInput = getEl<HTMLInputElement>("passwordInput");
const firstNameInput = getEl<HTMLInputElement>("firstNameInput");
const lastNameInput = getEl<HTMLInputElement>("lastNameInput");

const loginButton = getEl<HTMLButtonElement>("loginButton");
const registerPullupButton = getEl<HTMLButtonElement>("registerPullupButton");
const registerSendButton = getEl<HTMLButtonElement>("registerSendButton");
const goBackButton = getEl<HTMLButtonElement>("goBackButton");

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

    goBackButton.addEventListener("click", () => {
        void showRegisterFields(false);
    });
}

function redirectIfLoggedIn(): void {
    let token = localStorage.getItem("token");
    if(token){
        window.location.replace("/src/pages/main.html");
    }
}

function showRegisterFields(showRegister: boolean): void{
    displayHTMLElement(goBackButton, showRegister);
    displayHTMLElement(registerPullupButton, !showRegister);
    displayHTMLElement(accountText, !showRegister);
    displayHTMLElement(loginButton, !showRegister);
    displayHTMLElement(firstNameInput, showRegister);
    displayHTMLElement(lastNameInput, showRegister);
    displayHTMLElement(registerSendButton, showRegister);
}

// Button presses

function registerPullupPress(): void {
    showRegisterFields(true);

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

        // Successful registration
        if (response.status === 200) {
            const data = await response.json();

            localStorage.setItem("token", data.token);

            window.location.replace("/src/pages/loginRegister.html");
            return;
        }

        if (response.status === 400) {
            showError("loginError", "Bitte fülle alle Felder aus.");
            return;
        }

        if (response.status === 403) {
            showError("loginError", "Ein Account mit dieser E-Mail existiert bereits.");
            return;
        }

        if (response.status === 500) {
            showError("loginError", "Interner Serverfehler.");
            return;
        }

        console.error("Unexpected status:", response.status);
        showError("loginError", "Ein unbekannter Fehler ist aufgetreten.");

    } catch (error) {
        console.error("Could not reach backend:", error);
        showError("loginError", "Server konnte nicht erreicht werden");
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

            showError("loginError", "Login fehlgeschlagen");
        }

    } catch (error) {
        console.error("Backend could not be reached:", error);

        showError("loginError", "Server konnte nicht erreicht werden");
    }
}