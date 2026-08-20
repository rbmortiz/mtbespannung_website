import { manageNavBarLinks, redirectIfNoToken, setFirstName } from "./HelperFunctions";

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(): Promise<void>{
    manageNavBarLinks();
    redirectIfNoToken();
    setFirstName();
}