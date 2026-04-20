import { useState } from 'react'
import './NavBar.css'
import Aboutme from './websites/Aboutme'
import Infos from './websites/Infos'
import Bespannungen from './websites/Bespannungen'
import Rootwebsite from './websites/Rootwebsite'
import Trainerstunden from './websites/Trainerstunden'

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
            <nav className="navbar navbar-expand-lg navbar-dark bg-dark" id="nb">
                <a className="navbar-brand" href="#" onClick={() => {siteSwitcherHandler(0)}}>mtBespannung</a>
                <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav">
                        <li className="nav-item">
                            <a className={'nav-link' + (currentSite===1 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(1)}}>Bespannungsservice</a>
                        </li>
                        <li className="nav-item">
                            <a className={'nav-link' + (currentSite===2 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(2)}}>Bespannungsinfos</a>
                        </li>
                        <li className="nav-item">
                            <a className={'nav-link' + (currentSite===3 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(3)}}>Trainerstunden</a>
                        </li>
                        <li className="nav-item">
                            <a className={'nav-link' + (currentSite===4 ? ' active' : '')} href="#" onClick={() => {siteSwitcherHandler(4)}}>Über-mich</a>
                        </li>
                    </ul>
                </div>
            </nav>
            <div id="content">
                {pages[currentSite]}
            </div>
        </>
    );
}

export default NavBar;