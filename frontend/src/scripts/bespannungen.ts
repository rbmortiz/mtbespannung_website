import { manageNavBarLinks, setFirstName } from "./HelperFunctions";

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
} else {
    init();
}

async function init(): Promise<void>{
    manageNavBarLinks();
    setFirstName();
}