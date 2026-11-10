import {
	manageNavBarLinks,
	setFirstName,
	initRacketBackground,
	redirectIfNoToken
} from "./helpers/HelperFunctions";

if (document.readyState === "loading") {
	document.addEventListener("DOMContentLoaded", init);
} else {
	init();
}

async function init(): Promise<void> {
	manageNavBarLinks();
	initRacketBackground();
	redirectIfNoToken();
	await setFirstName();
}
