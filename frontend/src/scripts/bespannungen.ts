import { manageNavBarLinks, setFirstName, initRacketBackground } from "./HelperFunctions";

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(): Promise<void>{
    manageNavBarLinks();
    initRacketBackground();
    await setFirstName();
}