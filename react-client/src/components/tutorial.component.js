import React, { Component } from "react";
import TutorialDataService from "../services/tutorial.service";
import { withRouter } from '../common/with-router';

class Tutorial extends Component {
  constructor(props) {
    super(props);
    this.onChangeTitle = this.onChangeTitle.bind(this);
    this.onChangeDescription = this.onChangeDescription.bind(this);
    this.getTutorial = this.getTutorial.bind(this);
    this.updatePublished = this.updatePublished.bind(this);
    this.updateTutorial = this.updateTutorial.bind(this);
    this.deleteTutorial = this.deleteTutorial.bind(this);

    this.state = {
      currentTutorial: {
        id: null,
        title: "",
        description: "",
        published: false
      },
      message: "",
      // BUG #13 FIX: Add loading state
      isLoading: false,
      error: null
    };
  }

  componentDidMount() {
    this.getTutorial(this.props.router.params.id);
  }

  onChangeTitle(e) {
    const title = e.target.value;
    this.setState(function(prevState) {
      return {
        currentTutorial: {
          ...prevState.currentTutorial,
          title: title
        },
        error: null
      };
    });
  }

  onChangeDescription(e) {
    const description = e.target.value;
    this.setState(prevState => ({
      currentTutorial: {
        ...prevState.currentTutorial,
        description: description
      },
      error: null
    }));
  }

  getTutorial(id) {
    // BUG #13 FIX: Set loading state before API call
    this.setState({ isLoading: true, error: null });

    TutorialDataService.get(id)
      .then(response => {
        this.setState({
          currentTutorial: response.data,
          isLoading: false,
          error: null
        });
        console.log(response.data);
      })
      .catch(e => {
        console.error("Error fetching tutorial:", e);
        this.setState({
          isLoading: false,
          error: "Failed to load tutorial. Please try again."
        });
      });
  }

  updatePublished(status) {
    // BUG #13 FIX: Set loading state before API call
    this.setState({ isLoading: true, error: null, message: "" });

    var data = {
      id: this.state.currentTutorial.id,
      title: this.state.currentTutorial.title,
      description: this.state.currentTutorial.description,
      published: status
    };

    TutorialDataService.update(this.state.currentTutorial.id, data)
      .then(response => {
        // BUG #10 FIX: Use server response as source of truth
        // Previous code used optimistic update which could cause state inconsistency
        this.setState({
          currentTutorial: response.data,
          message: "The status was updated successfully!",
          isLoading: false,
          error: null
        });
        console.log(response.data);
      })
      .catch(e => {
        console.error("Error updating published status:", e);
        
        let errorMessage = "Failed to update status. Please try again.";
        if (e.response && e.response.data && typeof e.response.data === 'string') {
          errorMessage = e.response.data;
        }
        
        this.setState({
          isLoading: false,
          error: errorMessage,
          message: ""
        });
      });
  }

  updateTutorial() {
    const { title, description } = this.state.currentTutorial;

    // Client-side validation
    if (!title || title.trim().length === 0) {
      this.setState({ error: "Title is required and cannot be empty" });
      return;
    }

    if (!description || description.trim().length === 0) {
      this.setState({ error: "Description is required and cannot be empty" });
      return;
    }

    // BUG #13 FIX: Set loading state before API call
    this.setState({ isLoading: true, error: null, message: "" });

    TutorialDataService.update(
      this.state.currentTutorial.id,
      this.state.currentTutorial
    )
      .then(response => {
        console.log(response.data);
        this.setState({
          currentTutorial: response.data,
          message: "The tutorial was updated successfully!",
          isLoading: false,
          error: null
        });
      })
      .catch(e => {
        console.error("Error updating tutorial:", e);
        
        let errorMessage = "Failed to update tutorial. Please try again.";
        if (e.response && e.response.data && typeof e.response.data === 'string') {
          errorMessage = e.response.data;
        }
        
        this.setState({
          isLoading: false,
          error: errorMessage,
          message: ""
        });
      });
  }

  deleteTutorial() {
    // BUG #13 FIX: Set loading state before API call
    this.setState({ isLoading: true, error: null });

    TutorialDataService.delete(this.state.currentTutorial.id)
      .then(response => {
        console.log(response.data);
        this.props.router.navigate('/tutorials');
      })
      .catch(e => {
        console.error("Error deleting tutorial:", e);
        this.setState({
          isLoading: false,
          error: "Failed to delete tutorial. Please try again."
        });
      });
  }

  render() {
    const { currentTutorial, message, isLoading, error } = this.state;

    // BUG #11 FIX: Verify tutorial.id exists before rendering edit form
    if (!currentTutorial || !currentTutorial.id) {
      return (
        <div>
          {isLoading && (
            <div className="alert alert-info" role="alert">
              Loading tutorial...
            </div>
          )}
          
          {error && (
            <div className="alert alert-danger" role="alert">
              {error}
            </div>
          )}
          
          {!isLoading && !error && (
            <div className="alert alert-warning" role="alert">
              Tutorial not found or still loading...
            </div>
          )}
        </div>
      );
    }

    return (
      <div>
        {error && (
          <div className="alert alert-danger" role="alert">
            {error}
          </div>
        )}

        {message && (
          <div className="alert alert-success" role="alert">
            {message}
          </div>
        )}

        <div className="edit-form">
          <h4>Tutorial</h4>
          <form>
            <div className="form-group">
              <label htmlFor="title">Title</label>
              <input
                type="text"
                className="form-control"
                id="title"
                value={currentTutorial.title}
                onChange={this.onChangeTitle}
                disabled={isLoading}
              />
            </div>
            <div className="form-group">
              <label htmlFor="description">Description</label>
              <input
                type="text"
                className="form-control"
                id="description"
                value={currentTutorial.description}
                onChange={this.onChangeDescription}
                disabled={isLoading}
              />
            </div>

            <div className="form-group">
              <label>
                <strong>Status:</strong>
              </label>
              {currentTutorial.published ? " Published" : " Pending"}
            </div>
          </form>

          {currentTutorial.published ? (
            <button
              className="badge badge-primary mr-2"
              onClick={() => this.updatePublished(false)}
              disabled={isLoading}
            >
              UnPublish
            </button>
          ) : (
            <button
              className="badge badge-primary mr-2"
              onClick={() => this.updatePublished(true)}
              disabled={isLoading}
            >
              Publish
            </button>
          )}

          <button
            className="badge badge-danger mr-2"
            onClick={this.deleteTutorial}
            disabled={isLoading}
          >
            Delete
          </button>

          <button
            type="submit"
            className="badge badge-success"
            onClick={this.updateTutorial}
            disabled={isLoading}
          >
            {isLoading ? "Updating..." : "Update"}
          </button>
        </div>
      </div>
    );
  }
}

export default withRouter(Tutorial);
