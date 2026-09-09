import { useEffect, useState } from "react";
import api from "./services/api";
import "./App.css";

function getErrorMessage(error) {
  if (!error.response) {
    return "Unable to connect to the backend. Please make sure the Spring Boot server is running.";
  }

  const backendMessage = error.response.data?.message;
  if (backendMessage) {
    return backendMessage;
  }

  switch (error.response.status) {
    case 400:
      return "Maximum participant limit has been reached.";
    case 404:
      return "The requested resource was not found.";
    case 409:
      return "Officer is already nominated for this training programme.";
    case 500:
      return "A server error occurred. Please try again.";
    default:
      return "Something went wrong. Please try again.";
  }
}

function getList(response) {
  return Array.isArray(response.data) ? response.data : response.data?.content || [];
}

function App() {
  const [departments, setDepartments] = useState([]);
  const [officers, setOfficers] = useState([]);
  const [trainings, setTrainings] = useState([]);
  const [nominations, setNominations] = useState([]);
  const [form, setForm] = useState({
    departmentId: "",
    officerId: "",
    trainingId: "",
  });
  const [message, setMessage] = useState({ type: "", text: "" });
  const [loading, setLoading] = useState(true);

  const loadNominations = async () => {
    const response = await api.get("/nominations");
    setNominations(getList(response));
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        const [departmentResponse, officerResponse, trainingResponse, nominationResponse] =
          await Promise.all([
            api.get("/departments"),
            api.get("/officers"),
            api.get("/trainings"),
            api.get("/nominations"),
          ]);

        setDepartments(getList(departmentResponse));
        setOfficers(getList(officerResponse));
        setTrainings(getList(trainingResponse));
        setNominations(getList(nominationResponse));
      } catch (error) {
        setMessage({ type: "error", text: getErrorMessage(error) });
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
    setMessage({ type: "", text: "" });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.departmentId || !form.officerId || !form.trainingId) {
      setMessage({ type: "error", text: "Please select all fields." });
      return;
    }

    try {
      await api.post("/nominations", {
        officerId: Number(form.officerId),
        trainingId: Number(form.trainingId),
        departmentId: Number(form.departmentId),
      });
      await loadNominations();
      setForm({ departmentId: "", officerId: "", trainingId: "" });
      setMessage({ type: "success", text: "Officer nominated successfully." });
    } catch (error) {
      setMessage({ type: "error", text: getErrorMessage(error) });
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this nomination?")) {
      return;
    }

    try {
      await api.delete(`/nominations/${id}`);
      await loadNominations();
      setMessage({ type: "success", text: "Nomination deleted successfully." });
    } catch (error) {
      setMessage({ type: "error", text: getErrorMessage(error) });
    }
  };

  return (
    <main className="container">
      <header>
        <h1>Government Training Management System</h1>
        <h2>Training Officer Nomination</h2>
      </header>

      <section className="card">
        <h3>Nomination Form</h3>
        <form onSubmit={handleSubmit}>
          <label>
            Department
            <select name="departmentId" value={form.departmentId} onChange={handleChange}>
              <option value="">Select department</option>
              {departments.map((department) => (
                <option key={department.id} value={department.id}>
                  {department.name}
                </option>
              ))}
            </select>
          </label>

          <label>
            Officer
            <select name="officerId" value={form.officerId} onChange={handleChange}>
              <option value="">Select officer</option>
              {officers.map((officer) => (
                <option key={officer.id} value={officer.id}>
                  {officer.name} - {officer.employeeId}
                </option>
              ))}
            </select>
          </label>

          <label>
            Training Programme
            <select name="trainingId" value={form.trainingId} onChange={handleChange}>
              <option value="">Select training programme</option>
              {trainings.map((training) => (
                <option key={training.id} value={training.id}>
                  {training.title}
                </option>
              ))}
            </select>
          </label>

          <button type="submit">Nominate Officer</button>
        </form>
        {message.text && <p className={`message ${message.type}`}>{message.text}</p>}
      </section>

      <section className="card">
        <h3>Current Nominations</h3>
        {loading ? (
          <p>Loading nominations...</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Officer</th>
                  <th>Employee ID</th>
                  <th>Training</th>
                  <th>Department</th>
                  <th>Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {nominations.length === 0 ? (
                  <tr>
                    <td colSpan="7">No nominations found.</td>
                  </tr>
                ) : (
                  nominations.map((nomination) => (
                    <tr key={nomination.id}>
                      <td>{nomination.id}</td>
                      <td>{nomination.officer?.name || "-"}</td>
                      <td>{nomination.officer?.employeeId || "-"}</td>
                      <td>{nomination.training?.title || "-"}</td>
                      <td>{nomination.department?.name || "-"}</td>
                      <td>{nomination.nominationDate || nomination.date || "-"}</td>
                      <td>
                        <button
                          className="delete-button"
                          type="button"
                          onClick={() => handleDelete(nomination.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}

export default App;
