export function getEl<T extends HTMLElement>(id: string): T {
    const el = document.getElementById(id);

    if (!el) {
        throw new Error(`Expected element #${id} to exist in login.html`);
    }

    return el as T;
}

export function manageNavBarLinks(): void {

    let loginLink = getEl<HTMLLIElement>("loginLink");
    let dashboardLink = getEl<HTMLLIElement>("dashboardLink");
    let bespannungenLink = getEl<HTMLLIElement>("bespannungenLink");
    let trainerstundenLink = getEl<HTMLLIElement>("trainerstundenLink");
    let mainLink = getEl<HTMLLIElement>("mainLink");

    let path = window.location.pathname;
    let token = localStorage.getItem("token");

    if(token){
        loginLink.classList.add("d-none");
        console.log("User is logged in")
    }
    else {
        dashboardLink.classList.add("d-none");
        console.log("User is not logged in");
    }
    
    if(path.includes("loginRegister.html")){
        dashboardLink.classList.add("d-none");
        trainerstundenLink.classList.add("d-none");
        bespannungenLink.classList.add("d-none");
        loginLink.classList.add("d-none");
    }
    else if(path.includes("bespannungen.html")){
        bespannungenLink.classList.add("d-none");
    }
    else if(path.includes("trainerstunden.html")){
        trainerstundenLink.classList.add("d-none");
    }
    else if(path.includes("dashboard.html")){
        dashboardLink.classList.add("d-none");
        loginLink.classList.add("d-none");
    }
    else if(path.includes("main.html")){
        mainLink.classList.add("d-none");
    }
}

export function redirectIfNoToken(): void {
    let token = localStorage.getItem("token");
    if(!token) window.location.replace("/src/pages/main.html");
}

export async function setFirstName(): Promise<void> {
    const userText = getEl<HTMLAnchorElement>("userText");
    let token = localStorage.getItem("token");

    if(!token)return;

    try {
        const response = await fetch(
            "https://api.mtbespannung.de/getUser",
            {
                method: "GET",

                headers: {
                    "Authorization": `Bearer ${token}`
                }
            }
        );

        if (response.ok) {
            const data = await response.json();
            userText.innerHTML = `/${data.firstName}`;
            console.log("username updated");
        } else {
            console.log("Not Valid First Name found");
        }

    } catch (error) {
        console.error("Backend could not be reached:", error);
    }
}