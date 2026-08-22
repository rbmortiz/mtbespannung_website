import { initRacketBackground, manageNavBarLinks } from "./HelperFunctions"

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(){
    initRacketBackground();
    manageNavBarLinks();
}   