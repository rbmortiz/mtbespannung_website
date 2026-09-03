import {
    manageNavBarLinks,
    showError,
    redirectIfNoToken,
    setFirstName,
    initRacketBackground,
    getEl,
} from "./helpers/HelperFunctions";
import type { StringTypes, StringingOrder } from "./helpers/interfaces";

const emailInput = getEl<HTMLInputElement>("emailField");
const passwordInput = getEl<HTMLInputElement>("passwordField");
const firstNameInput = getEl<HTMLInputElement>("firstName");
const lastNameInput = getEl<HTMLInputElement>("lastName");
const deleteAccountButton = getEl<HTMLDivElement>("deleteAccountButton");
const logoutButton = getEl<HTMLButtonElement>("logoutButton");
const saveButton = getEl<HTMLButtonElement>("saveButton");

const patchOrderButton = getEl<HTMLButtonElement>("patchOrderButton");
const deleteOrderButton = getEl<HTMLButtonElement>("deleteOrderButton");

const stringingTable = getEl<HTMLTableSectionElement>("stringingTable");

let email: String;
let firstName: String;
let lastName: String;
let role: String;

let stringTypes: StringTypes[] = [];
let userStrings: StringingOrder[] = [];

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(): Promise<void> {
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
        console.log("updating user");
        void updateUser();
    });

    patchOrderButton.addEventListener("click", () => {
        console.log("updating order");
        void updateOrderInformation();
    });

    deleteOrderButton.addEventListener("click", () => {
        console.log("deleting order");
        void deleteStringingOrder();
    });

    if (role === "admin") buildForAdmin();
    else buildForUser();

    setPlaceholderItems();
}

async function setUserCredentials(): Promise<Boolean> {
    const token = `Bearer ${localStorage.getItem("token")}`;

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/getUserInformation",
            {
                method: "GET",

                headers: {
                    Authorization: token,
                },
            },
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
        showError("dashboardError", "Server konnte nicht erreicht werden");
    }

    return false;
}

function buildForAdmin(): void { }

async function buildForUser(): Promise<void> {
    var token = localStorage.getItem("token");

    if (token === undefined || token === null) {
        showError("dashboardError", "Konto wurde nicht gefunden");
        console.error("No token was found");
        return;
    }

    await getStringTypes(token);
    await getUserStrings(token);

    insertStringingTable();
}

async function getStringTypes(token: String): Promise<void> {
    try {
        const response = await fetch("https://api.mtbespannung.de/getStrings", {
            method: "GET",

            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        if (response.status === 200) {
            stringTypes = (await response.json()) as StringTypes[];
            return;
        }

        if (response.status === 304) {
            console.log("No Strings could be found");
        }

        if (response.status === 500) {
            console.error("Database error, Strings could not be fetched");
        }
    } catch (error) {
        console.error("Could not reach backend: ", error);
        showError("dashboardError", "Server konnte nicht erreicht werden");
    }
}

async function deleteStringingOrder() {
    try {
        const token = localStorage.getItem("token");

        var orderId: number = Number(
            getEl<HTMLSpanElement>("modalRacketName").getAttribute("order-id"),
        );

        const response = await fetch("https://api.mtbespannung.de/deleteStringingOrder",
            {
                method: "DELETE",

                headers: {
                    Authorization: `Bearer ${token}`,
                },

                body: JSON.stringify({
                    "stringingOrderId": orderId
                })
            } 
        );

        if(response.ok){
            window.location.reload();
        }

        if(response.status === 403){
            showError("dashboardError", "Bespannung ist in Bearbeitung/Bespannt/Zugestellt");
            console.error("Database error");
        }

        if(response.status === 404){
            showError("dashboardError", "Account/Bespannung nicht gefunden");
            console.error("Database error");
        }

        if(response.status === 401){
            showError("dashboardError", "Bitte neu anmelden");
            console.error("Database error");
        }

        if(response.status === 500){
            console.error("Database error");
        }
    } catch (error) {
        console.error(error);
        console.log("Server error");
    }
}
async function getUserStrings(token: String): Promise<void> {
    try {
        const response = await fetch(
            "https://api.mtbespannung.de/getUserStringingJobs",
            {
                method: "POST",

                headers: {
                    Authorization: `Bearer ${token}`
                },
            },
        );

        if (response.status === 200) {
            userStrings = (await response.json()) as StringingOrder[];
            return;
        }

        if (response.status === 304) {
            console.log("No Stringing Orders found");
        }

        if (response.status === 500) {
            console.error("Serverfehler bei Benutzerbesaitungen anzeigen lassen");
        }

        if(response.status === 401){
            showError("dashboardError", "Bitte neu anmelden");
            console.error("Database error");
        }
    } catch (error) {
        console.error("Could not reach backend: ", error);
        showError("dashboardError", "Server konnte nicht erreicht werden");
    }
}

function setPlaceholderItems(): void {
    emailInput.placeholder = "" + (email === undefined ? "E-Mail" : email);
    firstNameInput.placeholder =
        "" + (firstName === undefined ? "Vorname" : firstName);
    lastNameInput.placeholder =
        "" + (lastName === undefined ? "Nachname" : lastName);
}

async function deleteUserAccount(): Promise<void> {
    const token = localStorage.getItem("token");

    if (!token) {
        showError("dashboardError", "Du bist nicht angemeldet.");
        return;
    }

    try {
        const response = await fetch("https://api.mtbespannung.de/deleteUser", {
            method: "DELETE",
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });

        if (response.status === 200) {
            localStorage.removeItem("token");
            window.location.replace("/src/pages/loginRegister.html");
            return;
        }

        if (response.status === 401) {
            showError("dashboardError", "Account wurde nicht gefunden.");
            return;
        }

        if (response.status === 403) {
            showError("dashboardError", "Ungültige Benutzerdaten.");
            return;
        }

        showError("dashboardError", "Account konnte nicht gelöscht werden");
    } catch (error) {
        console.error("Could not reach backend:", error);
        showError("dashboardError", "Server konnte nicht erreicht werden");
    }
}

function logout(): void {
    localStorage.removeItem("token");
    window.location.replace("/src/pages/loginRegister.html");
}

async function updateUser(): Promise<void> {
    const token = localStorage.getItem("token");

    if (!token) {
        showError("dashboardError", "Du bist nicht angemeldet.");
        return;
    }

    const newMail = emailInput.value;
    const newPassword = passwordInput.value;
    const newFirstName = firstNameInput.value;
    const newLastName = lastNameInput.value;

    try {
        const response = await fetch("https://api.mtbespannung.de/editUser", {
            method: "PATCH",

            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
            },

            body: JSON.stringify({
                email: newMail,
                firstName: newFirstName,
                lastName: newLastName,
                password: newPassword,
            }),
        });

        if (response.status === 200) {
            const data = await response.json();
            localStorage.setItem("token", data.token);
            window.location.href = "/src/pages/dashboard.html";
            return;
        }

        if (response.status === 400) {
            showError("dashboardError", "Fehlende Daten.");
            return;
        }

        if (response.status === 401) {
            showError("dashboardError", "Token ist nicht mehr gültig.");
            return;
        }

        if (response.status === 404) {
            showError("dashboardError", "Benutzer wurde nicht gefunden.");
            return;
        }

        if (response.status === 409) {
            showError("dashboardError", "Neue Email ist bereits vergeben.");
            return;
        }

        showError("dashboardError", "Account konnte nicht verändert werden");
    } catch (error) {
        console.error("Could not reach backend:", error);
        showError("dashboardError", "Server konnte nicht erreicht werden");
    }
}

function insertStringingTable(): void {
    stringingTable.innerHTML = "";

    for (const jsonObj of userStrings) {
        insertSingleEntry(jsonObj);
    }

    addMoreInfoListeners();
}

function insertSingleEntry(order: StringingOrder): void {
    var created_at = new Date(order.created_at);

    stringingTable.innerHTML += `
        <tr>
            <td>${created_at.toLocaleDateString("de-DE")}</td>
            <td>${order.racket_name}</td>
            <td><button class="btn btn-primary moreInfoButton" data-order-id="${order.order_id}" data-bs-toggle="modal" data-bs-target="#stringingModal">Mehr Details</button></td>
        </tr>
    `;
}

function addMoreInfoListeners(): void {
    const buttons =
        document.querySelectorAll<HTMLButtonElement>(".moreInfoButton");

    for (const button of buttons) {
        button.addEventListener("click", () => {
            const orderId = Number(button.dataset.orderId);

            const order = userStrings.find((order) => order.order_id === orderId);

            if (!order) {
                return;
            }

            showOrderDetails(order);
        });
    }
}

function showOrderDetails(order: StringingOrder): void {
    const created = new Date(order.created_at);
    const updated = new Date(order.updated_at);

    getEl<HTMLSpanElement>("modalRacketName").innerHTML = order.racket_name;
    getEl<HTMLSpanElement>("modalRacketName").setAttribute(
        "order-id",
        order.order_id.toString(),
    );
    getEl<HTMLSpanElement>("modalCreatedAt").innerHTML =
        created.toLocaleDateString("de-DE");
    getEl<HTMLSpanElement>("modalUpdatedAt").innerHTML =
        updated.toLocaleDateString("de-DE");

    getEl<HTMLInputElement>("modalVerticalKG").value = "";
    getEl<HTMLInputElement>("modalHorizontalKG").value = "";
    getEl<HTMLInputElement>("modalInfos").value = "";

    getEl<HTMLInputElement>("modalVerticalKG").placeholder = order.vertical_kg === null ? "" : order.vertical_kg.toString();
    getEl<HTMLInputElement>("modalHorizontalKG").placeholder = order.horizontal_kg === null ? "" : order.horizontal_kg.toString();

    getEl<HTMLInputElement>("modalInfos").placeholder = (order.additional_info === null || order.additional_info === undefined) ? "" : order.additional_info;

    getEl<HTMLSpanElement>("modalString").innerHTML = order.string_id.toString();


    getEl<HTMLSpanElement>("modalStatus").classList.remove(
        "text-secondary",
        "text-warning",
        "text-info",
        "text-success"
    );

    switch (order.status) {
        case "pending":
            getEl<HTMLSpanElement>("modalStatus").innerHTML = "Unerledigt";
            getEl<HTMLSpanElement>("modalStatus").classList.add("text-secondary");
            break;
        case "in_progress":
            getEl<HTMLSpanElement>("modalStatus").innerHTML = "In Bearbeitung";
            getEl<HTMLSpanElement>("modalStatus").classList.add("text-warning");
            break;
        case "completed":
            getEl<HTMLSpanElement>("modalStatus").innerHTML = "Bespannt";
            getEl<HTMLSpanElement>("modalStatus").classList.add("text-info");
            break;
        case "delivered":
            getEl<HTMLSpanElement>("modalStatus").innerHTML = "Zugestellt";
            getEl<HTMLSpanElement>("modalStatus").classList.add("text-success");
            break;
        default:
            break;
    }

    getEl<HTMLSpanElement>("modalString").innerHTML = getNameOfString(
        order.string_id,
    );
}

function getNameOfString(id: number): string {
    const racketString = stringTypes.find(
        (racketString) => racketString.string_id === id,
    );

    if (racketString === null || racketString === undefined) return "";

    return racketString.name;
}

async function updateOrderInformation(): Promise<void> {
    const token = localStorage.getItem("token");

    var infos: string = getEl<HTMLInputElement>("modalInfos").value;
    var orderId: number = Number(
        getEl<HTMLSpanElement>("modalRacketName").getAttribute("order-id"),
    );

    const kgVertInput = getEl<HTMLInputElement>("modalVerticalKG").value;

    const kgHorInput = getEl<HTMLInputElement>("modalHorizontalKG").value;

    const kgVert: number | null = kgVertInput === "" ? null : Number(kgVertInput);

    const kgHor: number | null = kgHorInput === "" ? null : Number(kgHorInput);

    try {
        const response = await fetch("https://api.mtbespannung.de/updateOrder", {
            method: "PATCH",

            headers: {
                Authorization: `Bearer ${token}`,
                "Content-Type": "application/json",
            },

            body: JSON.stringify({
                order_id: orderId,
                kgVert: kgVert,
                kgHor: kgHor,
                infos: infos,
            }),
        });

        if (response.status === 400) {
            showError("patchOrderError", "Fehlende Daten");
            return;
        }

        if (response.status === 404) {
            showError("patchOrderError", "Bespannungsorder existiert nicht");
            return;
        }

        if (response.status === 401) {
            showError("patchOrderError", "Bitte neu anmelden");
            return;
        }

        if (response.status === 403) {
            showError("patchOrderError", "Bespannungsorder kann nicht mehr verändert werden",);
            return;
        }

        if (response.status === 500) {
            showError("patchOrderError", "Datenbankfehler");
            return;
        }

        window.location.reload();
    } catch (error) {
        console.error(error);
        showError("dashboardError", "Server konnte nicht erreicht werden");
    }
}