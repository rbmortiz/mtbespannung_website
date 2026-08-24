import { manageNavBarLinks, redirectIfNoToken, setFirstName, initRacketBackground } from "./HelperFunctions";

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
}