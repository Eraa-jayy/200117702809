import { useEffect, useMemo, useState } from "react";
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
  const [rules, setRules] = useState({});
  const [selectedTraining, setSelectedTraining] = useState(null);
  const [ruleForm, setRuleForm] = useState({ ruleType: "DEPARTMENT", ruleValue: "" });
  const [editingRuleId, setEditingRuleId] = useState(null);
  const [eligibility, setEligibility] = useState(null);
  const [eligibilityLoading, setEligibilityLoading] = useState(false);
  const [form, setForm] = useState({
    departmentId: "",
    officerId: "",
    trainingId: "",
  });
  const [message, setMessage] = useState({ type: "", text: "" });
  const [loading, setLoading] = useState(true);
  const trainingSummaries = useMemo(
    () => trainings.map((training) => {
      const trainingNominations = nominations.filter(
        (nomination) => nomination.training?.id === training.id
      );
      return {
        ...training,
        confirmed: trainingNominations.filter((nomination) => nomination.status === "CONFIRMED").length,
        waiting: trainingNominations.filter((nomination) => nomination.status === "WAITING").length,
      };
    }),
    [trainings, nominations]
  );
  const sortedNominations = useMemo(
    () => [...nominations].sort((first, second) => {
      const trainingOrder = (first.training?.title || "").localeCompare(
        second.training?.title || ""
      );
      if (trainingOrder !== 0) {
        return trainingOrder;
      }
      return (first.nominationDate || "").localeCompare(second.nominationDate || "");
    }),
    [nominations]
  );
  const recentParticipationByOfficer = useMemo(() => {
    const cutoff = new Date();
    cutoff.setMonth(cutoff.getMonth() - 12);
    const now = new Date();
    const participation = {};

    nominations
      .filter((nomination) => nomination.status === "CONFIRMED" && nomination.nominationDate)
      .filter((nomination) => {
        const date = new Date(nomination.nominationDate);
        return date >= cutoff && date <= now;
      })
      .sort((first, second) => (
        new Date(second.nominationDate) - new Date(first.nominationDate)
      ))
      .forEach((nomination) => {
        const officerId = nomination.officer?.id;
        if (!officerId) {
          return;
        }
        if (!participation[officerId]) {
          participation[officerId] = [];
        }
        participation[officerId].push({
          title: nomination.training?.title || "Training programme",
          date: nomination.nominationDate,
        });
      });

    return participation;
  }, [nominations]);

  const loadNominations = async () => {
    const response = await api.get("/nominations");
    setNominations(getList(response));
  };

  const loadRules = async (trainingId) => {
    const response = await api.get(`/eligibility-rules/training/${trainingId}`);
    setRules((current) => ({ ...current, [trainingId]: getList(response) }));
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

  useEffect(() => {
    trainings.forEach((training) => {
      if (rules[training.id] === undefined) {
        loadRules(training.id).catch((error) => {
          setMessage({ type: "error", text: getErrorMessage(error) });
        });
      }
    });
  }, [trainings]);

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value });
    setMessage({ type: "", text: "" });
    if (event.target.name === "officerId" || event.target.name === "trainingId") {
      setEligibility(null);
    }
  };

  const checkEligibility = async () => {
    if (!form.officerId || !form.trainingId) {
      setEligibility(null);
      return;
    }
    setEligibilityLoading(true);
    try {
      const response = await api.get("/nominations/eligibility", {
        params: { officerId: form.officerId, trainingId: form.trainingId },
      });
      setEligibility(response.data);
    } catch (error) {
      setEligibility({ eligible: false, reason: getErrorMessage(error) });
    } finally {
      setEligibilityLoading(false);
    }
  };

  useEffect(() => {
    checkEligibility();
  }, [form.officerId, form.trainingId]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    if (!form.departmentId || !form.officerId || !form.trainingId) {
      setMessage({ type: "error", text: "Please select all fields." });
      return;
    }

    try {
      const response = await api.post("/nominations", {
        officerId: Number(form.officerId),
        trainingId: Number(form.trainingId),
        departmentId: Number(form.departmentId),
      });
      await loadNominations();
      setForm({ departmentId: "", officerId: "", trainingId: "" });
      setMessage({
        type: "success",
        text: response.data?.status === "WAITING"
          ? "Training is full. Officer has been added to the waiting list."
          : "Officer nominated successfully and confirmed.",
      });
    } catch (error) {
      setMessage({ type: "error", text: getErrorMessage(error) });
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Are you sure you want to cancel this nomination?")) {
      return;
    }

    try {
      const response = await api.patch(`/nominations/${id}/cancel`);
      await loadNominations();
      setMessage({
        type: "success",
        text: response.data?.promoted
          ? "Nomination cancelled. The next waiting participant has been promoted."
          : "Nomination cancelled successfully.",
      });
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

          <button type="submit" disabled={eligibilityLoading || eligibility?.eligible === false}>
            {eligibility?.eligible === false ? "Registration Not Allowed" : "Nominate Officer"}
          </button>
        </form>
        <button className="secondary-button" type="button" onClick={checkEligibility}>
          Check Eligibility
        </button>
        {form.officerId && form.trainingId && (
          <div className="eligibility-summary">
            <h4>Eligibility Check</h4>
            {(() => {
              const selectedOfficer = officers.find(
                (officer) => String(officer.id) === String(form.officerId)
              );
              const selectedTraining = trainings.find(
                (training) => String(training.id) === String(form.trainingId)
              );
              return (
                <p>
                  Officer: {selectedOfficer?.name || "-"} | Training: {selectedTraining?.title || "-"} |
                  Department: {selectedOfficer?.department?.name || "-"} | Grade: {selectedOfficer?.grade || "-"} |
                  Years of service: {selectedOfficer?.yearsOfService ?? "-"}
                </p>
              );
            })()}
            <p>
              {eligibilityLoading
                ? "Checking eligibility..."
                : eligibility?.eligible
                  ? "✓ Eligible for this training."
                  : `✗ Not eligible: ${eligibility?.reason || "Eligibility could not be confirmed."}`}
            </p>
            {eligibility?.lastParticipationDate && (
              <p>
                Last participation: {eligibility.lastParticipationDate.replace("T", " ")}.
                Registration is not allowed until the 12-month restriction has passed.
              </p>
            )}
          </div>
        )}
        {message.text && <p className={`message ${message.type}`}>{message.text}</p>}
      </section>

      <section className="card">
        <h3>Officer Eligibility Information</h3>
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Officer</th>
                <th>Employee ID</th>
                <th>Department</th>
                <th>Grade</th>
                <th>Years of Service</th>
                <th>Programmes Participated in Last 12 Months</th>
              </tr>
            </thead>
            <tbody>
              {officers.map((officer) => (
                <tr key={officer.id}>
                  <td>{officer.name}</td>
                  <td>{officer.employeeId}</td>
                  <td>{officer.department?.name || "-"}</td>
                  <td>{officer.grade || "-"}</td>
                  <td>{officer.yearsOfService ?? "-"}</td>
                  <td>
                    {(recentParticipationByOfficer[officer.id] || []).length === 0 ? (
                      "None"
                    ) : (
                      <ul className="participation-list">
                        {recentParticipationByOfficer[officer.id].map((participation) => (
                          <li key={`${participation.title}-${participation.date}`}>
                            {participation.title} ({participation.date.replace("T", " ")})
                          </li>
                        ))}
                      </ul>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="card">
        <h3>Training Programmes</h3>
        <div className="table-wrapper">
          <table>
            <thead>
              <tr>
                <th>Training</th>
                <th>Capacity</th>
                <th>Confirmed</th>
                <th>Waiting</th>
                <th>Eligibility</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {trainingSummaries.length === 0 ? (
                <tr>
                  <td colSpan="6">No training programmes found.</td>
                </tr>
              ) : (
                trainingSummaries.map((training) => (
                  <tr key={training.id}>
                    <td>{training.title}</td>
                    <td>{training.maximumParticipants}</td>
                    <td>{training.confirmed}</td>
                    <td>{training.waiting}</td>
                    <td>
                      {rules[training.id] === undefined
                        ? "Not loaded"
                        : rules[training.id].length
                          ? `${rules[training.id].length} rule${rules[training.id].length === 1 ? "" : "s"}`
                          : "All officers eligible"}
                    </td>
                    <td>
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={async () => {
                          setSelectedTraining(training);
                          await loadRules(training.id);
                        }}
                      >
                        Manage Eligibility
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      {selectedTraining && (
        <section className="card">
          <div className="section-heading">
            <h3>Eligibility Rules: {selectedTraining.title}</h3>
            <button type="button" className="secondary-button" onClick={() => setSelectedTraining(null)}>
              Close
            </button>
          </div>
          <div className="rule-form">
            <label>
              Rule Type
              <select
                value={ruleForm.ruleType}
                onChange={(event) => setRuleForm({ ...ruleForm, ruleType: event.target.value })}
              >
                <option value="DEPARTMENT">Department</option>
                <option value="GRADE">Grade</option>
                <option value="MIN_YEARS_SERVICE">Minimum Years of Service</option>
              </select>
            </label>
            <label>
              Value
              {ruleForm.ruleType === "DEPARTMENT" ? (
                <select
                  value={ruleForm.ruleValue}
                  onChange={(event) => setRuleForm({ ...ruleForm, ruleValue: event.target.value })}
                >
                  <option value="">Select department</option>
                  {departments.map((department) => (
                    <option key={department.id} value={department.name}>{department.name}</option>
                  ))}
                </select>
              ) : (
                <input
                  type={ruleForm.ruleType === "MIN_YEARS_SERVICE" ? "number" : "text"}
                  min="0"
                  value={ruleForm.ruleValue}
                  onChange={(event) => setRuleForm({ ...ruleForm, ruleValue: event.target.value })}
                  placeholder={ruleForm.ruleType === "MIN_YEARS_SERVICE" ? "5" : "Senior Officer"}
                />
              )}
            </label>
            <button
              type="button"
              onClick={async () => {
                try {
                  const request = editingRuleId
                    ? api.put(`/eligibility-rules/${editingRuleId}`, ruleForm)
                    : api.post("/eligibility-rules", {
                      ...ruleForm,
                      trainingId: selectedTraining.id,
                    });
                  await request;
                  await loadRules(selectedTraining.id);
                  setRuleForm({ ...ruleForm, ruleValue: "" });
                  setEditingRuleId(null);
                  setMessage({ type: "success", text: editingRuleId ? "Eligibility rule updated." : "Eligibility rule added." });
                } catch (error) {
                  setMessage({ type: "error", text: getErrorMessage(error) });
                }
              }}
            >
              Add Rule
            </button>
          </div>
          <ul className="rules-list">
            {(rules[selectedTraining.id] || []).length === 0 ? (
              <li>✓ All officers are eligible.</li>
            ) : (rules[selectedTraining.id] || []).map((rule) => (
              <li key={rule.id}>
                <span>{rule.ruleType.replaceAll("_", " ")}: {rule.ruleValue}</span>
                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => {
                    setEditingRuleId(rule.id);
                    setRuleForm({ ruleType: rule.ruleType, ruleValue: rule.ruleValue });
                  }}
                >
                  Edit
                </button>
                <button
                  type="button"
                  className="delete-button"
                  onClick={async () => {
                    await api.delete(`/eligibility-rules/${rule.id}`);
                    await loadRules(selectedTraining.id);
                    if (editingRuleId === rule.id) {
                      setEditingRuleId(null);
                      setRuleForm({ ruleType: "DEPARTMENT", ruleValue: "" });
                    }
                  }}
                >
                  Remove
                </button>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="card">
        <h3>Current Nominations</h3>
        {loading ? (
          <p>Loading nominations...</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Training Programme</th>
                  <th>Officer</th>
                  <th>Employee ID</th>
                  <th>Department</th>
                  <th>Status</th>
                  <th>Nomination Date</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {nominations.length === 0 ? (
                  <tr>
                    <td colSpan="7">No nominations found.</td>
                  </tr>
                ) : (
                  sortedNominations.map((nomination) => (
                    <tr key={nomination.id}>
                      <td>{nomination.training?.title || "-"}</td>
                      <td>{nomination.officer?.name || "-"}</td>
                      <td>{nomination.officer?.employeeId || "-"}</td>
                      <td>{nomination.department?.name || "-"}</td>
                      <td>
                        <span className={`status ${nomination.status?.toLowerCase() || ""}`}>
                          {nomination.status || "-"}
                        </span>
                      </td>
                      <td>{(nomination.nominationDate || nomination.date || "-").replace("T", " ")}</td>
                      <td>
                        {nomination.status !== "CANCELLED" && (
                          <button
                            className="delete-button"
                            type="button"
                            onClick={() => handleCancel(nomination.id)}
                          >
                            Cancel
                          </button>
                        )}
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
