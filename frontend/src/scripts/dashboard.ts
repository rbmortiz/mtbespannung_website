import {
	manageNavBarLinks,
	showError,
	redirectIfNoToken,
	setFirstName,
	initRacketBackground,
	getEl
} from "./helpers/HelperFunctions";
import type { StringTypes, StringingOrder, users } from "./helpers/interfaces";

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
const tableCard = getEl<HTMLDivElement>("tableCard");

const adminFields = getEl<HTMLDivElement>("adminFields");
let adminFirstNameInput: HTMLSpanElement;
let adminLastNameInput: HTMLSpanElement;
let adminEmailInput: HTMLSpanElement;
let adminStatusInput: HTMLSelectElement;
let adminStringInput: HTMLSelectElement;
let adminSearchBarInput: HTMLInputElement;

let email: String;
let firstName: String;
let lastName: String;
let role: String;

let stringTypes: StringTypes[] = [];
let userStrings: StringingOrder[] = [];
let users: users[] = [];

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
		localStorage.removeItem("token");
		window.location.replace("/src/pages/loginRegister.html");
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
					Authorization: token
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
		showError("dashboardError", "Server konnte nicht erreicht werden");
	}

	return false;
}

async function adminGetUserStringingOrders(token: String): Promise<void> {
	try {
		const response = await fetch(
			"https://api.mtbespannung.de/getAllStringingOrders",
			{
				method: "GET",

				headers: {
					Authorization: `Bearer ${token}`
				}
			}
		);

		if (response.status === 200) {
			userStrings = (await response.json()) as StringingOrder[];
			console.log(userStrings[0]);
			return;
		}

		if (response.status === 304) {
			console.log("No Stringing Orders found");
		}

		if (response.status === 500) {
			console.error(
				"Serverfehler bei Benutzerbesaitungen anzeigen lassen"
			);
		}

		if (response.status === 401) {
			showError("dashboardError", "Bitte neu anmelden");
			console.error("Database error");
		}

		showNoStringingOrders();
	} catch (error) {
		console.error("Could not reach backend: ", error);
		showError("dashboardError", "Server konnte nicht erreicht werden");
		showNoStringingOrders();
	}
}

async function adminGetUsers(token: String): Promise<void> {
	try {
		const response = await fetch(
			"https://api.mtbespannung.de/getAllUsers",
			{
				method: "GET",

				headers: {
					Authorization: `Bearer ${token}`
				}
			}
		);

		if (response.status === 200) {
			users = (await response.json()) as users[];
			console.log(users[0]);
			return;
		}

		if (response.status === 304) {
			console.log("No Users Orders found");
		}

		if (response.status === 500) {
			console.error("Serverfehler bei Benutzern anzeigen lassen");
		}

		if (response.status === 401) {
			showError("dashboardError", "Bitte neu anmelden");
			console.error("Database error");
		}
	} catch (error) {
		console.error("Could not reach backend: ", error);
		showError("dashboardError", "Server konnte nicht erreicht werden");
	}
}

async function getStringTypes(token: String): Promise<void> {
	try {
		const response = await fetch("https://api.mtbespannung.de/getStrings", {
			method: "GET",

			headers: {
				Authorization: `Bearer ${token}`
			}
		});

		if (response.status === 200) {
			stringTypes = (await response.json()) as StringTypes[];
			console.log(stringTypes.at(0));
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
			getEl<HTMLSpanElement>("modalRacketName").getAttribute("order-id")
		);

		const response = await fetch(
			"https://api.mtbespannung.de/deleteStringingOrder",
			{
				method: "DELETE",

				headers: {
					Authorization: `Bearer ${token}`
				},

				body: JSON.stringify({
					stringingOrderId: orderId
				})
			}
		);

		if (response.ok) {
			window.location.reload();
		}

		if (response.status === 403) {
			showError(
				"dashboardError",
				"Bespannung ist in Bearbeitung/Bespannt/Zugestellt"
			);
			console.error("Database error");
		}

		if (response.status === 404) {
			showError("dashboardError", "Account/Bespannung nicht gefunden");
			console.error("Database error");
		}

		if (response.status === 401) {
			showError("dashboardError", "Bitte neu anmelden");
			console.error("Database error");
		}

		if (response.status === 500) {
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
			"https://api.mtbespannung.de/getUserStringingOrders",
			{
				method: "POST",

				headers: {
					Authorization: `Bearer ${token}`
				}
			}
		);

		if (response.status === 200) {
			userStrings = (await response.json()) as StringingOrder[];
			return;
		}

		if (response.status === 304) {
			console.log("No Stringing Orders found");
		}

		if (response.status === 500) {
			console.error(
				"Serverfehler bei Benutzerbesaitungen anzeigen lassen"
			);
		}

		if (response.status === 401) {
			showError("dashboardError", "Bitte neu anmelden");
			console.error("Database error");
		}

		showNoStringingOrders();
	} catch (error) {
		console.error("Could not reach backend: ", error);
		showError("dashboardError", "Server konnte nicht erreicht werden");
		showNoStringingOrders();
	}
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
				Authorization: `Bearer ${token}`
			}
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

async function updateOrderInformation(): Promise<void> {
	const token = localStorage.getItem("token");
	var bodyData: string;

	var infos: string = getEl<HTMLInputElement>("modalInfos").value;

	var orderId: number = Number(
		getEl<HTMLSpanElement>("modalRacketName").getAttribute("order-id")
	);

	const kgVertInput = getEl<HTMLInputElement>("modalVerticalKG").value;

	const kgHorInput = getEl<HTMLInputElement>("modalHorizontalKG").value;

	const kgVert: number | null =
		kgVertInput === "" ? null : Number(kgVertInput);

	const kgHor: number | null = kgHorInput === "" ? null : Number(kgHorInput);

	if (role === "admin") {
		const stringData: number = Number(adminStringInput.value);

		const statusData = adminStatusInput.value;

		bodyData = JSON.stringify({
			order_id: orderId,
			kgVert: kgVert,
			kgHor: kgHor,
			infos: infos,
			status: statusData,
			stringId: stringData
		});
	} else {
		bodyData = JSON.stringify({
			order_id: orderId,
			kgVert: kgVert,
			kgHor: kgHor,
			infos: infos
		});
	}

	try {
		const response = await fetch(
			"https://api.mtbespannung.de/updateOrder",
			{
				method: "PATCH",

				headers: {
					Authorization: `Bearer ${token}`,
					"Content-Type": "application/json"
				},

				body: bodyData
			}
		);

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
			showError(
				"patchOrderError",
				"Bespannungsorder kann nicht mehr verändert werden"
			);
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
				"Content-Type": "application/json"
			},

			body: JSON.stringify({
				email: newMail,
				firstName: newFirstName,
				lastName: newLastName,
				password: newPassword
			})
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

async function buildForAdmin(): Promise<void> {
	var token = localStorage.getItem("token");

	if (token === undefined || token === null) {
		showError("dashboardError", "Konto wurde nicht gefunden");
		console.error("No token was found");
		return;
	}

	await getStringTypes(token);
	await adminGetUserStringingOrders(token);
	await adminGetUsers(token);

	alterAdminFields();
	insertStringingTable();
}

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
	setInfoFields();
}

function setInfoFields(): void {
	const header = getEl<HTMLDivElement>("infoModalHeader");
	const body = getEl<HTMLDivElement>("infoModalBody");

	getEl<HTMLLabelElement>("infoRacketName").addEventListener("click", () => {
		header.innerHTML = `
			Schlägername
		`;
		body.innerHTML = `
			Hier erscheint der Name den sie bei der Erstellung des Auftrags angegeben haben. Dies ist der Name des Schlägers, oder eine grobe Beschreibung des Schlägers.
		`;
	});

	getEl<HTMLLabelElement>("infoKG").addEventListener("click", () => {
		header.innerHTML = `
			Besaitungshärte
		`;
		body.innerHTML = `
			Die Schlägerhärte setzt sich zusammen aus der Härte der Quergezogenen einzelnen Saiten und aus der Härte der Längsgezogenen einzelnen Saiten. <br><br>

			Wenn ein Schläger <span class="text-warning">fest</span> bespannt ist, dann gewinnt man Präzision, verliert aber Geschwindigkeit aus dem Schlag. <br><br>

			Wenn ein Schläger <span class="text-success">weich</span> bespannt ist, verliert man Präzision, bekommt aber mehr Geschwindigkeit aus dem Schlag. <br><br>

			Falls sie sich mit den Härten nicht auskennen, können sie gerne in das <span class="text-info">Info</span> Feld ihre Präferenz schreiben ("Etwas fester", "Etwas weicher", "Eher ausgeglichen").
		`;
	});

	getEl<HTMLLabelElement>("infoString").addEventListener("click", () => {
		header.innerHTML = `
			Saite
		`;
		body.innerHTML = `
			Dies ist die Saite die sie bei der Bespannung ausgewählt haben. <br><br>
			Möchten sie diese im nachhinein ändern, so kontaktieren sie mich bitte unter <span class="text-success">moritz@mtbespannung.de</span>
		`;
	});

	getEl<HTMLLabelElement>("infoInfos").addEventListener("click", () => {
		header.innerHTML = `
			Infos
		`;
		body.innerHTML = `
			Hier haben sie zusätzliche Informationen zur Bespannung angegeben. <br><br>
			Solange Der Status noch <span class="text-secondary">Unerledigt</span> ist, können sie diesen Verändern.
		`;
	});

	getEl<HTMLLabelElement>("infoStatus").addEventListener("click", () => {
		header.innerHTML = `
			Status
		`;
		body.innerHTML = `
			Der Status besteht aus vier Zuständen, welche die momentane Bearbeitungsstufe ihrer Bespannung festlegen. <br><br>
			<span class="text-secondary">Unerledigt</span>: Die Bespannung wurde erst abgegeben, und noch nicht angefangen. <br><br>
			<span class="text-warning">In Bearbeitung</span>: Der Schläger wird in diesem Moment bespannt, und es können keine Details mehr verändert werden. <br><br>
			<span class="text-info">Bespannt</span>: Der Schläger ist vollständig bespannt, wurde aber noch nicht im Sportpoint zum abholen hinterlegt. <br><br>
			<span class="text-success">Zugestellt</span>: Der Schläger ist vollständig bespannt und wurde im Sportpoint zum abholen hinterlegt. 
		`;
	});

	getEl<HTMLLabelElement>("infoPrice").addEventListener("click", () => {
		header.innerHTML = `
			Preis
		`;
		body.innerHTML = `
			Dies ist der Preis der Bespannung, welcher sich aus <span class="text-info">15€</span> Besaitungskosten, und den <span class="text-info">Saitenkosten</span> zusammensetzt.
		`;
	});

	getEl<HTMLLabelElement>("infoCreationDate").addEventListener(
		"click",
		() => {
			header.innerHTML = `
			Erstellungsdatum
		`;
			body.innerHTML = `
			An diesem Tag ist die Bespannung in das System eingegangen.
		`;
		}
	);

	getEl<HTMLLabelElement>("infoUpdatedDate").addEventListener("click", () => {
		header.innerHTML = `
			Aktualisierungsdatum
		`;
		body.innerHTML = `
			Sobald sich etwas an ihrer Bespannung verändert, egal ob von ihnen oder dem Bespanner, wird das Datum aktualisiert.
		`;
	});
}

function alterAdminFields(): void {
	adminFields.innerHTML += `
        <div class="d-flex align-items-center gap-3 mb-2">
            <label for="adminFirstNameInput"
                class="form-label mb-0 text-nowrap d-flex"
                style="min-width: 100%;">
                Vorname:
                <span class="text-success ms-auto" id="adminFirstNameInput"></span>
            </label>
        </div>

        <div class="d-flex align-items-center gap-3 mb-2">
            <label for="adminLastNameInput"
                class="form-label mb-0 text-nowrap d-flex"
                style="min-width: 100%;">
                Nachname:
                <span class="text-success ms-auto" id="adminLastNameInput"></span>
            </label>
        </div>

        <div class="d-flex align-items-center gap-3 mb-2">
            <label for="adminEmailInput"
                class="form-label mb-0 text-nowrap d-flex"
                style="min-width: 100%;">
                E-Mail:
                <span class="text-success ms-auto" id="adminEmailInput"></span>
            </label>
        </div>
    `;

	getEl<HTMLDivElement>("statusContainer").innerHTML = `
		<label for="modalStatus" class="form-label mb-0" style="min-width: 130px">
			Status:
		</label>

		<select class="form-select" id="adminStatus">
		</select>
	`;

	getEl<HTMLDivElement>("stringContainer").innerHTML = `
		<label for="modalStatus" class="form-label mb-0" style="min-width: 130px">
			Saite: 
		</label>

		<select class="form-select" id="adminString">
			
		</select>
	`;

	getEl<HTMLDivElement>("searchBarContainer").innerHTML = `
		<input
			type="search"
			class="form-control"
			id="searchInput"
			placeholder="Suchen..."
			aria-label="Suchen"
			style="border: none; border-radius: 0px"
		/>
	`;

	getEl<HTMLTableSectionElement>("tableHeader").innerHTML = `
		<tr>
			<th scope="col">Erstellt</th>
			<th scope="col">Name</th>
			<th scope="col">Schlägername</th>
			<th scope="col">Status</th>
			<th scope="col"></th>
		</tr>
	`;

	adminFirstNameInput = getEl<HTMLSpanElement>("adminFirstNameInput");
	adminLastNameInput = getEl<HTMLSpanElement>("adminLastNameInput");

	adminEmailInput = getEl<HTMLSpanElement>("adminEmailInput");
	adminStatusInput = getEl<HTMLSelectElement>("adminStatus");
	adminStringInput = getEl<HTMLSelectElement>("adminString");

	adminSearchBarInput = getEl<HTMLInputElement>("searchInput");

	adminSearchBarInput.addEventListener("input", () => {
		void filterStringingTable();
	});
}

function showNoStringingOrders(): void {
	tableCard.classList.add("d-flex");
	tableCard.innerHTML = `
        <h2 class="text-secondary my-5 align-items-center justify-content-center text-center w-100">Keine Bespannungen gefunden</h2>
    `;
}

function setPlaceholderItems(): void {
	emailInput.placeholder = "" + (email === undefined ? "E-Mail" : email);
	firstNameInput.placeholder =
		"" + (firstName === undefined ? "Vorname" : firstName);
	lastNameInput.placeholder =
		"" + (lastName === undefined ? "Nachname" : lastName);
}

function insertStringingTable(): void {
	stringingTable.innerHTML = "";

	for (const jsonObj of userStrings) {
		insertSingleEntry(jsonObj);
	}

	addMoreInfoListeners();
}

function filterStringingTable(): void {
	stringingTable.innerHTML = "";

	const searchValue = adminSearchBarInput.value.toLowerCase().trim();

	for (const strOrder of userStrings) {
		if (
			strOrder.customer_email
				?.toLowerCase()
				.trim()
				.includes(searchValue) ||
			strOrder.customer_first_name
				?.toLowerCase()
				.trim()
				.includes(searchValue) ||
			strOrder.customer_last_name
				?.toLowerCase()
				.trim()
				.includes(searchValue) ||
			strOrder.racket_name.toLowerCase().trim().includes(searchValue) ||
			strOrder.additional_info
				?.toLowerCase()
				.trim()
				.includes(searchValue) ||
			getTransformedStatus(strOrder.status)
				.toLowerCase()
				.trim()
				.includes(searchValue)
		) {
			insertSingleEntry(strOrder);
		}
	}

	addMoreInfoListeners();
}

function getTransformedStatus(status: string) {
	let temp: string = "";

	switch (status) {
		case "pending":
			temp = "Unerledigt";
			break;
		case "in_progress":
			temp = "In Bearbeitung";
			break;
		case "completed":
			temp = "Bespannt";
			break;
		case "delivered":
			temp = "Zugestellt";
			break;
		default:
			break;
	}

	return temp;
}

function insertSingleEntry(order: StringingOrder): void {
	var created_at = new Date(order.created_at);

	if (role === "admin") {
		stringingTable.innerHTML += `
			<tr>
				<td>${created_at.toLocaleDateString("de-DE")}</td>
				<td>${order.customer_first_name}, ${order.customer_last_name}</td>
				<td>${order.racket_name}</td>
				<td>${getTransformedStatus(order.status)}</td>
				<td><button class="btn btn-primary moreInfoButton" data-order-id="${order.order_id}" data-bs-toggle="modal" data-bs-target="#stringingModal">Mehr Details</button></td>
			</tr>
		`;
	} else {
		stringingTable.innerHTML += `
			<tr>
				<td>${created_at.toLocaleDateString("de-DE")}</td>
				<td>${order.racket_name}</td>
				<td><button class="btn btn-primary moreInfoButton" data-order-id="${order.order_id}" data-bs-toggle="modal" data-bs-target="#stringingModal">Mehr Details</button></td>
			</tr>
		`;
	}
}

function addMoreInfoListeners(): void {
	const buttons =
		document.querySelectorAll<HTMLButtonElement>(".moreInfoButton");

	for (const button of buttons) {
		button.addEventListener("click", () => {
			const orderId = Number(button.dataset.orderId);

			const order = userStrings.find(
				(order) => order.order_id === orderId
			);

			if (!order) {
				return;
			}

			showOrderDetails(order);
		});
	}
}

function showOrderDetails(order: StringingOrder): void {
	console.log(order);

	const created = new Date(order.created_at);
	const updated = new Date(order.updated_at);

	getEl<HTMLSpanElement>("modalRacketName").innerHTML = order.racket_name;
	getEl<HTMLSpanElement>("modalRacketName").setAttribute(
		"order-id",
		order.order_id.toString()
	);
	getEl<HTMLSpanElement>("modalCreatedAt").innerHTML =
		created.toLocaleDateString("de-DE");
	getEl<HTMLSpanElement>("modalUpdatedAt").innerHTML =
		updated.toLocaleDateString("de-DE");

	getEl<HTMLInputElement>("modalVerticalKG").value = "";
	getEl<HTMLInputElement>("modalHorizontalKG").value = "";
	getEl<HTMLInputElement>("modalInfos").value = "";

	getEl<HTMLInputElement>("modalVerticalKG").placeholder =
		order.vertical_kg === null ? "" : order.vertical_kg.toString();
	getEl<HTMLInputElement>("modalHorizontalKG").placeholder =
		order.horizontal_kg === null ? "" : order.horizontal_kg.toString();

	getEl<HTMLInputElement>("modalInfos").placeholder =
		order.additional_info === null || order.additional_info === undefined
			? ""
			: order.additional_info;

	getEl<HTMLSpanElement>("modalPrice").innerHTML =
		"" + order.price?.toString();

	if (role === "user") {
		getEl<HTMLSpanElement>("modalString").innerHTML = getNameOfString(
			order.string_id
		);

		getEl<HTMLSpanElement>("modalStatus").classList.remove(
			"text-secondary",
			"text-warning",
			"text-info",
			"text-success"
		);

		switch (order.status) {
			case "pending":
				getEl<HTMLSpanElement>("modalStatus").innerHTML = "Unerledigt";
				getEl<HTMLSpanElement>("modalStatus").classList.add(
					"text-secondary"
				);
				break;
			case "in_progress":
				getEl<HTMLSpanElement>("modalStatus").innerHTML =
					"In Bearbeitung";
				getEl<HTMLSpanElement>("modalStatus").classList.add(
					"text-warning"
				);
				break;
			case "completed":
				getEl<HTMLSpanElement>("modalStatus").innerHTML = "Bespannt";
				getEl<HTMLSpanElement>("modalStatus").classList.add(
					"text-info"
				);
				break;
			case "delivered":
				getEl<HTMLSpanElement>("modalStatus").innerHTML = "Zugestellt";
				getEl<HTMLSpanElement>("modalStatus").classList.add(
					"text-success"
				);
				break;
			default:
				break;
		}
	} else if (role === "admin") {
		adminStringInput.innerHTML = "";

		for (const string of stringTypes) {
			if (order.string_id === string.string_id) {
				adminStringInput.innerHTML += `
					<option
						data-price="${string.price}"
						value="${string.string_id}"
						selected
					>
						${string.name} (${string.price}€)
					</option>
				`;
			} else {
				adminStringInput.innerHTML += `
					<option
						data-price="${string.price}"
						value="${string.string_id}"
					>
						${string.name} (${string.price}€)
					</option>
				`;
			}
		}

		adminStatusInput.innerHTML = `
			<option value="pending" ${order.status === "pending" ? "selected" : ""}>Unerledigt</option>
			<option value="in_progress" ${order.status === "in_progress" ? "selected" : ""}>In Bearbeitung</option>
			<option value="completed" ${order.status === "completed" ? "selected" : ""}>Bespannt</option>
			<option value="delivered" ${order.status === "delivered" ? "selected" : ""}>Zugestellt</option>
		`;
	}

	if (
		adminFirstNameInput !== null &&
		adminFirstNameInput !== undefined &&
		adminLastNameInput !== null &&
		adminLastNameInput !== undefined &&
		adminEmailInput !== null &&
		adminEmailInput !== undefined
	) {
		if (
			order.customer_first_name === null &&
			order.customer_first_name === undefined &&
			order.customer_last_name === null &&
			order.customer_last_name === undefined &&
			order.customer_email === null &&
			order.customer_email === undefined
		) {
			for (const user of users) {
				if (user.user_id === order.user_id) {
					adminFirstNameInput.innerHTML = user.first_name;
					adminLastNameInput.innerHTML = user.last_name;
					adminEmailInput.innerHTML = user.email;
				}
			}

			console.log("successfully altered fields for admin");
		} else {
			adminFirstNameInput.innerHTML =
				order.customer_first_name !== null
					? order.customer_first_name
					: "";
			adminLastNameInput.innerHTML =
				order.customer_last_name !== null
					? order.customer_last_name
					: "";
			adminEmailInput.innerHTML =
				order.customer_email !== null ? order.customer_email : "";
		}
	}
}

function getNameOfString(id: number): string {
	const racketString = stringTypes.find(
		(racketString) => racketString.string_id === id
	);

	if (racketString === null || racketString === undefined) return "";

	return racketString.name;
}
