import { initRacketBackground, manageNavBarLinks, setFirstName } from "./helpers/HelperFunctions"

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(){
    initRacketBackground();
    manageNavBarLinks();
    setFirstName();
}   