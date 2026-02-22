import React, { Component } from "react";
import TutorialDataService from "../services/tutorial.service";
import { Link } from "react-router-dom";

export default class TutorialsList extends Component {
  constructor(props) {
    super(props);
    this.onChangeSearchTitle = this.onChangeSearchTitle.bind(this);
    this.retrieveTutorials = this.retrieveTutorials.bind(this);
    this.refreshList = this.refreshList.bind(this);
    this.setActiveTutorial = this.setActiveTutorial.bind(this);
    this.removeAllTutorials = this.removeAllTutorials.bind(this);
    this.searchTitle = this.searchTitle.bind(this);

    this.state = {
      tutorials: [],
      currentTutorial: null,
      currentIndex: -1,
      searchTitle: "",
      // BUG #13 FIX: Add loading state to show loading indicator
      isLoading: false,
      // BUG #8 FIX: Add error state for proper error handling
      error: null
    };
  }

  componentDidMount() {
    this.retrieveTutorials();
  }

  onChangeSearchTitle(e) {
    const searchTitle = e.target.value;
    this.setState({ searchTitle: searchTitle });
  }

  retrieveTutorials() {
    // BUG #13 FIX: Set loading state before API call
    this.setState({ isLoading: true, error: null });

    TutorialDataService.getAll()
      .then(response => {
        // BUG #8 FIX: Handle 204 No Content status code
        const tutorials = response.data || [];
        this.setState({ tutorials: tutorials, isLoading: false, error: null });
      })
      .catch(e => {
        // BUG #8 FIX: Properly handle errors and provide user feedback
        console.error("Error retrieving tutorials:", e);
        
        if (e.response && e.response.status === 204) {
          this.setState({ tutorials: [], isLoading: false, error: null });
        } else {
          this.setState({
            tutorials: [],
            isLoading: false,
            error: "Failed to load tutorials. Please try again later."
          });
        }
      });
  }

  refreshList() {
    this.retrieveTutorials();
    this.setState({ currentTutorial: null, currentIndex: -1 });
  }

  setActiveTutorial(tutorial, index) {
    this.setState({ currentTutorial: tutorial, currentIndex: index });
  }

  removeAllTutorials() {
    // BUG #13 FIX: Set loading state before API call
    this.setState({ isLoading: true, error: null });

    TutorialDataService.deleteAll()
      .then(response => {
        console.log(response.data);
        this.refreshList();
      })
      .catch(e => {
        // BUG #8 FIX: Properly handle errors
        console.error("Error deleting tutorials:", e);
        
        if (e.response && e.response.status === 204) {
          this.refreshList();
        } else {
          this.setState({
            isLoading: false,
            error: "Failed to delete tutorials. Please try again."
          });
        }
      });
  }

  searchTitle() {
    // BUG #13 FIX: Set loading state before API call
    this.setState({ 
      isLoading: true,
      error: null,
      currentTutorial: null,
      currentIndex: -1
    });

    TutorialDataService.findByTitle(this.state.searchTitle)
      .then(response => {
        // BUG #8 FIX: Handle 204 No Content status code
        const tutorials = response.data || [];
        this.setState({ tutorials: tutorials, isLoading: false, error: null });
      })
      .catch(e => {
        // BUG #8 FIX: Properly handle errors
        console.error("Error searching tutorials:", e);
        
        if (e.response && e.response.status === 204) {
          this.setState({ tutorials: [], isLoading: false, error: null });
        } else {
          this.setState({
            tutorials: [],
            isLoading: false,
            error: "Failed to search tutorials. Please try again."
          });
        }
      });
  }

  render() {
    const { searchTitle, tutorials, currentTutorial, currentIndex, isLoading, error } = this.state;

    return (
      <div className="list row">
        <div className="col-md-8">
          <div className="input-group mb-3">
            <input
              type="text"
              className="form-control"
              placeholder="Search by title"
              value={searchTitle}
              onChange={this.onChangeSearchTitle}
            />
            <div className="input-group-append">
              <button
                className="btn btn-outline-secondary"
                type="button"
                onClick={this.searchTitle}
                disabled={isLoading}
              >
                Search
              </button>
            </div>
          </div>
        </div>
        <div className="col-md-6">
          <h4>Tutorials List</h4>

          {isLoading && (
            <div className="alert alert-info" role="alert">
              Loading tutorials...
            </div>
          )}

          {error && (
            <div className="alert alert-danger" role="alert">
              {error}
            </div>
          )}

          {!isLoading && !error && (
            <ul className="list-group">
              {tutorials.length > 0 ? (
                tutorials.map((tutorial, index) => (
                  <li
                    className={
                      "list-group-item " +
                      (index === currentIndex ? "active" : "")
                    }
                    onClick={() => this.setActiveTutorial(tutorial, index)}
                    key={index}
                  >
                    {tutorial.title}
                  </li>
                ))
              ) : (
                <li className="list-group-item">No tutorials found</li>
              )}
            </ul>
          )}

          <button
            className="m-3 btn btn-sm btn-danger"
            onClick={this.removeAllTutorials}
            disabled={isLoading || tutorials.length === 0}
          >
            Remove All
          </button>
        </div>
        <div className="col-md-6">
          {currentTutorial ? (
            <div>
              <h4>Tutorial</h4>
              <div>
                <label>
                  <strong>Title:</strong>
                </label>{" "}
                {currentTutorial.title}
              </div>
              <div>
                <label>
                  <strong>Description:</strong>
                </label>{" "}
                {currentTutorial.description}
              </div>
              <div>
                <label>
                  <strong>Status:</strong>
                </label>{" "}
                {currentTutorial.published ? "Published" : "Pending"}
              </div>

              <Link
                to={"/tutorials/" + currentTutorial.id}
                className="badge badge-warning"
              >
                Edit
              </Link>
            </div>
          ) : (
            <div>
              <br />
              <p>Please click on a Tutorial...</p>
            </div>
          )}
        </div>
      </div>
    );
  }
}
