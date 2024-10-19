import { useEffect, useState } from "react";
import "./App.css";
import Header from "./components/Header";
import { getContacts } from "./api/ContactService";
import ContactList from "./components/ContactList";
import { Navigate, Route, Routes } from "react-router-dom";

function App() {
  const [data, setData] = useState({ content: [], totalPages: 0 });
  const [currentPage, setCurrentPage] = useState(0);

  const getAllContacts = async (page = 0, size = 10) => {
    try {
      setCurrentPage(page);
      const { data } = await getContacts(page, size);
      setData(data);
      console.log(data);
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    getAllContacts();
  }, []);

  const toggleModal = (isOpen) => {};
  console.log("App component rendered");

  return (
    <>
      <Header toggleModal={toggleModal} numberOfContacts={data.totalElements} />
      <main className="main">
        <div className="container">
          <Routes>
            <Route path="/" element={<Navigate to={"/contacts"} />} />
            <Route
              path="/contacts"
              element={
                <ContactList
                  data={data}
                  currentPage={currentPage}
                  getAllContacts={getAllContacts}
                />
              }
            />
          </Routes>
        </div>
      </main>
    </>
  );
}

export default App;
