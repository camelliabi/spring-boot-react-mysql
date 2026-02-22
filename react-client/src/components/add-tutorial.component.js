import React, { Component } from "react";
import TutorialDataService from "../services/tutorial.service";

export default class AddTutorial extends Component {
  constructor(props) {
    super(props);
    this.onChangeTitle = this.onChangeTitle.bind(this);
    this.onChangeDescription = this.onChangeDescription.bind(this);
    this.saveTutorial = this.saveTutorial.bind(this);
    this.newTutorial = this.newTutorial.bind(this);

    this.state = {
      id: null,
      title: "",
      description: "",
      published: false,
      submitted: false,
      // BUG #13 FIX: Add loading state
      isLoading: false,
      // BUG #9 FIX: Add error state for validation and API errors
      error: null
    };
  }

  onChangeTitle(e) {
    this.setState({
      title: e.target.value,
      error: null
    });
  }

  onChangeDescription(e) {
    this.setState({
      description: e.target.value,
      error: null
    });
  }

  saveTutorial() {
    // BUG #9 FIX: Add client-side validation before API call
    const { title, description } = this.state;

    if (!title || title.trim().length === 0) {
      this.setState({ error: "Title is required and cannot be empty" });
      return;
    }

    if (!description || description.trim().length === 0) {
      this.setState({ error: "Description is required and cannot be empty" });
      return;
    }

    // BUG #13 FIX: Set loading state before API call
    this.setState({ isLoading: true, error: null });

    var data = {
      title: title.trim(),
      description: description.trim()
    };

    TutorialDataService.create(data)
      .then(response => {
        this.setState({
          id: response.data.id,
          title: response.data.title,
          description: response.data.description,
          published: response.data.published,
          submitted: true,
          isLoading: false,
          error: null
        });
        console.log(response.data);
      })
      .catch(e => {
        // BUG #9 FIX: Handle errors and display to user
        console.error("Error creating tutorial:", e);
        
        let errorMessage = "Failed to create tutorial. Please try again.";
        
        if (e.response && e.response.data) {
          if (typeof e.response.data === 'string') {
            errorMessage = e.response.data;
          } else if (e.response.data.message) {
            errorMessage = e.response.data.message;
          }
        }
        
        this.setState({
          isLoading: false,
          error: errorMessage,
          submitted: false
        });
      });
  }

  newTutorial() {
    this.setState({
      id: null,
      title: "",
      description: "",
      published: false,
      submitted: false,
      isLoading: false,
      error: null
    });
  }

  render() {
    const { title, description, submitted, isLoading, error } = this.state;

    return (
      <div className="submit-form">
        {submitted ? (
          <div>
            <h4>You submitted successfully!</h4>
            <button className="btn btn-success" onClick={this.newTutorial}>
              Add
            </button>
          </div>
        ) : (
          <div>
            {error && (
              <div className="alert alert-danger" role="alert">
                {error}
              </div>
            )}

            <div className="form-group">
              <label htmlFor="title">Title</label>
              <input
                type="text"
                className="form-control"
                id="title"
                required
                value={title}
                onChange={this.onChangeTitle}
                name="title"
                disabled={isLoading}
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">Description</label>
              <input
                type="text"
                className="form-control"
                id="description"
                required
                value={description}
                onChange={this.onChangeDescription}
                name="description"
                disabled={isLoading}
              />
            </div>

            <button 
              onClick={this.saveTutorial} 
              className="btn btn-success"
              disabled={isLoading}
            >
              {isLoading ? "Submitting..." : "Submit"}
            </button>
          </div>
        )}
      </div>
    );
  }
}
