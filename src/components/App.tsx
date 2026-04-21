import Aboutme from './websites/Aboutme'
import Infos from './websites/Infos'
import Bespannungen from './websites/Bespannungen'
import Rootwebsite from './websites/Rootwebsite'
import Trainerstunden from './websites/Trainerstunden'

import { useState } from 'react'

function NavBar(){
    const [currentSite, siteSwitcherHandler] = useState(0);

    const pages = [
        <Rootwebsite />,
        <Bespannungen />,
        <Infos />,
        <Trainerstunden />,
        <Aboutme />
    ];

    return (
        <>
            <div className="allbox">
                <div className="leftbox">
                    <a className="navbar-name" href="#" onClick={() => {siteSwitcherHandler(0)}}>mtBespannung</a>
                </div>
                <nav className="rightbox">
                    <ul className="navigation-links-list">
                        <li className="list-link-item">
                            <a className={'navbar-link' + (currentSite===1 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(1)}}>Bespannungsservice</a>
                        </li>
                        <li className="list-link-item">
                            <a className={'navbar-link' + (currentSite===2 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(2)}}>Bespannungsinfos</a>
                        </li>
                        <li className="list-link-item">
                            <a className={'navbar-link' + (currentSite===3 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(3)}}>Trainerstunden</a>
                        </li>
                        <li className="list-link-item">
                            <a className={'navbar-link' + (currentSite===4 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(4)}}>Über-mich</a>
                        </li>
                    </ul>
                </nav>
            </div>
            <div id="main-content">
                {pages[currentSite]}
            </div>
        </>
    );
}

export default NavBar;