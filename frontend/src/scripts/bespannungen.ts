import { manageNavBarLinks, setFirstName, initRacketBackground, getEl, displayHTMLElement, showError } from "./helpers/HelperFunctions";
import type { StringTypes } from "./helpers/interfaces";

const infoTextHeader = getEl<HTMLHeadingElement>("infoTextHeader");
const infoTextBody = getEl<HTMLHeadingElement>("infoTextBody");

const token = localStorage.getItem("token");

// Form Input Elements

const firstNameInput = getEl<HTMLInputElement>("firstName");
const lastNameInput = getEl<HTMLInputElement>("lastName");
const emailInput = getEl<HTMLInputElement>("email");
const racketNameInput = getEl<HTMLInputElement>("racketName");
const racketTypeInput = getEl<HTMLSelectElement>("racketType");
const siteTypeInput = getEl<HTMLSelectElement>("siteType");
const horizontalKGInput = getEl<HTMLInputElement>("horizontalKG");
const verticalKGInput = getEl<HTMLInputElement>("verticalKG");
const infosInput = getEl<HTMLInputElement>("infos");
const priceTagInput = getEl<HTMLInputElement>("priceTag");

const sendOrderWithoutAccountButton = getEl<HTMLInputElement>("sendOrderWithoutAccount");
const sendOrderWithAccountButton = getEl<HTMLInputElement>("sendOrderWithAccount");

// Container Elements

const firstNameContainer = getEl<HTMLDivElement>("firstNameContainer");
const lastNameContainer = getEl<HTMLDivElement>("lastNameContainer");
const emailContainer = getEl<HTMLDivElement>("emailContainer");
const racketNameContainer = getEl<HTMLDivElement>("racketNameContainer");
const infosContainer = getEl<HTMLDivElement>("infosContainer");
const priceTagContainer = getEl<HTMLDivElement>("priceTagContainer");


let stringTypes: StringTypes[] = [];

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(): Promise<void>{
    setInfoFieldButtons();

    manageNavBarLinks();
    initRacketBackground();

    if(token === null || token === undefined){
        buildForGuestUser();
    } else {
        buildForUser();
    }

    await getStringTypes();
    await setFirstName();
}

function setInfoFieldButtons(): void {
    racketTypeInput.addEventListener("change", () => {
        siteTypeInput.innerHTML = "";
        addOptions(racketTypeInput.value);
    });

    siteTypeInput.addEventListener("change", () => {
        displayHTMLElement(priceTagContainer, true);
        calculatePriceOfStringingJob();
    });

    getEl<HTMLLabelElement>("firstNameInfo").addEventListener("click", () => {
        infoTextHeader.innerHTML = "Vorname";
        infoTextBody.innerHTML = `
            Hier können sie ihren Vornamen eintragen.
        `;
    });
    getEl<HTMLLabelElement>("lastNameInfo").addEventListener("click", () => {
        infoTextHeader.innerHTML = "Nachname";
        infoTextBody.innerHTML = `
            Hier können sie ihren Nachnamen eintragen.
        `;
    });
    getEl<HTMLLabelElement>("emailInfo").addEventListener("click", () => {
        infoTextHeader.innerHTML = "E-Mail";
        infoTextBody.innerHTML = `
            Hier können sie ihre E-Mail im Format <span class="text-success">example@example.de</span>
            eingeben. Die E-Mail wird zum Kontakt verwendet.
        `;
    });
    getEl<HTMLLabelElement>("racketNameInfo").addEventListener("click", () => {
        infoTextHeader.innerHTML = "Schlägername";
        infoTextBody.innerHTML = `
            Den Schlägernamen finden sie meistens auf dem <span class="text-success">Rahmen</span> ihres Schlägers,
            und er wird dafür verwendet den Schläger zuzuordnen. <br><br>
            Falls der Name nicht ersichtlich ist, reicht auch ein Anhaltspunkt oder eine Beschreibung des Schlägers aus.
        `;
    });
    getEl<HTMLLabelElement>("racketTypeInfo").addEventListener("click", () => {
        infoTextHeader.innerHTML = "Schlägertyp";
        infoTextBody.innerHTML = `
            Der Schlägertyp gibt an, ob es sich bei dem Auftrag um einen <span class="text-success">Badminton</span>- 
            oder einen <span class="text-success">Squash</span>schläger handelt.<br><br>
            Sie können dies im Dropdown-Menü auswählen.
        `;
    });
    getEl<HTMLLabelElement>("siteTypeInfo").addEventListener("click", () => {
        infoTextHeader.innerHTML = "Saite";
        infoTextBody.innerHTML = `
            Welche Saite brauche ich? Was ist der Unterschied zwischen all den Saiten? Lohnt sich eine Bespannung überhaupt?
            <br><br>
            Die Antworten auf solche Fragen finden sie in den <span class="text-success" type="button" id="goToInfos">Infos</span>
        `;
        getEl<HTMLSpanElement>("goToInfos").addEventListener("click", () => {
            window.location.href = "/src/pages/infos.html";
        });
    });
    getEl<HTMLLabelElement>("horizontalKGInfo").addEventListener("click", () => {
        void racketVHInfoText();
    });
    getEl<HTMLLabelElement>("verticalKGInfo").addEventListener("click", () => {
        void racketVHInfoText();
    });
    getEl<HTMLLabelElement>("infosInfo").addEventListener("click", () => {
        infoTextHeader.innerHTML = "Infos";
        infoTextBody.innerHTML = `
            Hier können sie zusätzliche <span class="text-success">Informationen/Präferenzen</span> eintragen, die für die Bespannung wichtig sind.
        `;
    });
}

function racketVHInfoText(): void {
    infoTextHeader.innerHTML = "Schlägerhärte";
    infoTextBody.innerHTML = `
        Die Schlägerhärte setzt sich zusammen aus der Härte der Quergezogenen einzelnen Saiten und aus der Härte der Längsgezogenen einzelnen Saiten. <br><br>

        Wenn ein Schläger <span class="text-warning">fest</span> bespannt ist, dann gewinnt man Präzision, verliert aber Geschwindigkeit aus dem Schlag. <br><br>

        Wenn ein Schläger <span class="text-success">weich</span> bespannt ist, verliert man Präzision, bekommt aber mehr Geschwindigkeit aus dem Schlag. <br><br>

        Falls sie sich mit den Härten nicht auskennen, können sie gerne in das <span class="text-info">Info</span> Feld ihre Präferenz schreiben ("Etwas fester", "Etwas weicher", "Eher ausgeglichen").
    `;
}

function buildForGuestUser(): void {
    sendOrderWithoutAccountButton.addEventListener("click", () => {
        void sendInput(racketNameInput.value, siteTypeInput.value, 
            verticalKGInput.value, horizontalKGInput.value, infosInput.value, firstNameInput.value, lastNameInput.value, emailInput.value);
    });
}

function buildForUser(): void {
    displayHTMLElement(sendOrderWithAccountButton, true);
    displayHTMLElement(sendOrderWithoutAccountButton, false);
    displayHTMLElement(firstNameContainer, false);
    displayHTMLElement(lastNameContainer, false);
    displayHTMLElement(emailContainer, false);

    racketNameContainer.classList.add("mt-5");
    infosContainer.classList.add("mb-5");

    sendOrderWithAccountButton.addEventListener("click", () => {
        void sendInput(racketNameInput.value, siteTypeInput.value, 
            verticalKGInput.value, horizontalKGInput.value, infosInput.value);
    });
}

async function getStringTypes(): Promise<void> {
    try {
        const response = await fetch("https://api.mtbespannung.de/getStrings", {
            method: "GET"
        });

        if(response.status === 200){
            stringTypes = await response.json() as StringTypes[];
            return;
        }

        if(response.status === 304){
            console.log("No Strings could be found");
        }

        if(response.status === 500){
            console.error("Database error, Strings could not be fetched");
        }
    } catch(error){
        console.error("Could not reach backend: ", error);
        showError("bespannungenError", "Server konnte nicht erreicht werden");
    }
}

function addOptions(sport: string): void {
    for(const str of stringTypes){
        if(str.sport.toLowerCase() === sport.toLowerCase()){
            siteTypeInput.innerHTML += `<option value="${str.string_id}">${str.name}<span class="text-secondary"> (${str.price}€)</span></option>`;
        }
    }
}

function calculatePriceOfStringingJob(): void {
    let site: string = (Number(siteTypeInput.value)+15).toString();
    priceTagInput.innerHTML = site;
}


// function overloading for the fetch request

async function sendInput(racketName: string, racketString: string, vertKG: string, 
    horKG: string, infos: string, firstName?: string, lastName?: string, email?:string): Promise<void>{

    if(firstName === undefined || lastName === undefined || email === undefined){
        const string_id: number = Number(racketString);

        try {
            const response = await fetch("https://api.mtbespannung.de/newStringingOrderAccount", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`
                },

                body: JSON.stringify({
                    "racket_name": racketName,
                    "horizontal_kg": horKG === "" ? null : Number(horKG),
                    "vertical_kg": vertKG === "" ? null : Number(vertKG),
                    "string_id": string_id, 
                    "additional_info": infos
                })
            })

            if(response.ok){
                window.location.replace("/src/pages/main.html");
            }

            if(response.status === 500){
                showError("bespannungenError", "Datenbank konnte nicht erreicht werden.");
            }

            if(response.status === 403){
                showError("bespannungenError", "Fehlende Daten");
            }

            if(response.status === 404){
                showError("bespannungenError", "Account konnte nicht gefunden werden");
            }

            if(response.status === 401){
                showError("bespannungenError", "Bitte neu anmelden");
            }

        } catch (error) { 
            console.error(error);
            showError("bespannungenError", "Server konnte nicht erreicht werden");
        }

        return;
    }

    const string_id: number = Number(racketString);

    try {
        const response = await fetch("https://api.mtbespannung.de/newStringingOrder", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },

            body: JSON.stringify({
                "racket_name": racketName,
                "horizontal_kg": horKG === "" ? null : Number(horKG),
                "vertical_kg": vertKG === "" ? null : Number(vertKG),
                "string_id": string_id, 
                "additional_info": infos,

                "customer_first_name": firstName,
                "customer_last_name": lastName,
                "customer_email": email
            })
        })

        if(response.ok){
            window.location.replace("/src/pages/main.html");
        }

        if(response.status === 500){
            showError("bespannungenError", "Datenbank konnte nicht erreicht werden.");
        }

        if(response.status === 403){
            showError("bespannungenError", "Fehlende Daten");
        }
        
    } catch (error) { 
        console.error(error);
        showError("bespannungenError", "Server konnte nicht erreicht werden");
    }
}