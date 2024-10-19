import React from "react";
import PropTypes from "prop-types";

const Header = ({ toggleModal, numberOfContacts }) => {
  console.log(numberOfContacts);
  return (
    <header className="header">
      <div className="container">
        <h3>Contact List ({numberOfContacts})</h3>
        <button className="btn" onClick={() => toggleModal(true)}>
          <i className="bi bi-plus-square"></i>
          Add new contact
        </button>
      </div>
    </header>
  );
};

Header.propTypes = {
  toggleModal: PropTypes.func,
  numberOfContacts: PropTypes.number,
};

export default Header;
