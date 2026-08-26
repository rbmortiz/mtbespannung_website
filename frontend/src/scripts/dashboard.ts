import { manageNavBarLinks, showError, redirectIfNoToken, setFirstName, initRacketBackground, getEl } from "./HelperFunctions";

const emailInput = getEl<HTMLInputElement>("emailField");
const passwordInput = getEl<HTMLInputElement>("passwordField");
const firstNameInput = getEl<HTMLInputElement>("firstName");
const lastNameInput = getEl<HTMLInputElement>("lastName");
const deleteAccountButton = getEl<HTMLDivElement>("deleteAccountButton");
const logoutButton = getEl<HTMLButtonElement>("logoutButton");
const saveButton = getEl<HTMLButtonElement>("saveButton");

let email: String;
let firstName: String;
let lastName: String;
let role: String;


if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(): Promise<void>{
    manageNavBarLinks();
    redirectIfNoToken();
    initRacketBackground();
    await setFirstName();
    await setUserCredentials();

    deleteAccountButton.addEventListener("click", () => {
        void deleteUserAccount();
    });

    logoutButton.addEventListener("click", () => {
        void logout();
    });

    saveButton.addEventListener("click", () => {
        void updateUser();
    });
    
    if(role === "admin")buildForAdmin();
    else buildForUser();

    setPlaceholderItems();
}

async function setUserCredentials(): Promise<Boolean>{
    const token = `Bearer ${localStorage.getItem("token")}`;

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/getUserInformation",
            {
                method: "GET",

                headers: {
                    "Authorization": token
                }
            }
        );

        if (response.status === 200) {
            const data = await response.json();

            email = data.email;
            firstName = data.firstName;
            lastName = data.lastName;
            role = data.role;
        }

    } catch (error) {
        console.error("Could not reach backend:", error);
        showError("Server konnte nicht erreicht werden");
    }

    return false;
}

function buildForAdmin(): void {

}

function buildForUser(): void {
    return;
}

function setPlaceholderItems(): void {
    emailInput.placeholder = "" + (email === undefined ? "E-Mail" : email);
    firstNameInput.placeholder = "" + (firstName === undefined ? "Vorname" : firstName);
    lastNameInput.placeholder = "" + (lastName === undefined ? "Nachname" : lastName);
}

async function deleteUserAccount(): Promise<void> {
    const token = localStorage.getItem("token");

    if (!token) {
        showError("Du bist nicht angemeldet.");
        return;
    }

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/deleteUser",
            {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }
        );

        if (response.status === 200) {
            localStorage.removeItem("token");
            window.location.replace("/src/pages/loginRegister.html");
            return;
        }

        if (response.status === 401) {
            showError("Account wurde nicht gefunden.");
            return;
        }

        if (response.status === 403) {
            showError("Ungültige Benutzerdaten.");
            return;
        }

        showError("Account konnte nicht gelöscht werden");

    } catch (error) {
        console.error("Could not reach backend:", error);
        showError("Server konnte nicht erreicht werden");
    }
}

function logout(): void {
    localStorage.removeItem("token");
    window.location.replace("/src/pages/loginRegister.html");
}

async function updateUser(): Promise<void> {
    const token = localStorage.getItem("token");

    if (!token) {
        showError("Du bist nicht angemeldet.");
        return;
    }

    const newMail = emailInput.value;
    const newPassword = passwordInput.value;
    const newFirstName = firstNameInput.value;
    const newLastName = lastNameInput.value;

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/editUser",
            {
                method: "PATCH",

                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    "email": newMail,
                    "firstName": newFirstName,
                    "lastName": newLastName,
                    "password": newPassword
                })
            }
        );

        if (response.status === 200) {
            const data = await response.json();
            localStorage.setItem("token", data.token);
            window.location.replace("/src/pages/loginRegister.html");
            return;
        }

        if (response.status === 400) {
            showError("Fehlende Daten.");
            return;
        }

        if (response.status === 401) {
            showError("Token ist nicht mehr gültig.");
            return;
        }

        if (response.status === 404) {
            showError("Benutzer wurde nicht gefunden.");
            return;
        }

        if (response.status === 409) {
            showError("Neue Email ist bereits vergeben.");
            return;
        }

        showError("Account konnte nicht verändert werden");

    } catch (error) {
        console.error("Could not reach backend:", error);
        showError("Server konnte nicht erreicht werden");
    }
}