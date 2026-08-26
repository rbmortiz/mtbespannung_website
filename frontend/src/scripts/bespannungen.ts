import { manageNavBarLinks, setFirstName, initRacketBackground, getEl } from "./HelperFunctions";

const infoTextHeader = getEl<HTMLHeadingElement>("infoTextHeader");
const infoTextBody = getEl<HTMLHeadingElement>("infoTextBody");

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(): Promise<void>{
    setInfoFieldButtons();

    manageNavBarLinks();
    initRacketBackground();
    await setFirstName();
}

function setInfoFieldButtons(): void {
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
            window.location.replace("/src/pages/infos.html")
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