import { manageNavBarLinks, showError, redirectIfNoToken, setFirstName, initRacketBackground, getEl } from "./HelperFunctions";

const emailInput = getEl<HTMLInputElement>("email");
const firstNameInput = getEl<HTMLInputElement>("firstName");
const lastNameInput = getEl<HTMLInputElement>("lastName");
const deleteAccountButton = getEl<HTMLDivElement>("deleteAccountButton");
const logoutButton = getEl<HTMLButtonElement>("logoutButton");

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
    })
    
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