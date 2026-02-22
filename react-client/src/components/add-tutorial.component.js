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
      // FIX ERR-011: Add error state for user feedback
      error: ""
    };
  }

  onChangeTitle(e) {
    this.setState({
      title: e.target.value
    });
  }

  onChangeDescription(e) {
    this.setState({
      description: e.target.value
    });
  }

  saveTutorial() {
    // FIX ERR-009: Add validation for both title and description
    // FIX ERR-011: Provide user feedback instead of silent console.error
    if (!this.state.title || this.state.title.trim() === "") {
      this.setState({ error: "Title is required and cannot be empty" });
      return;
    }
    if (!this.state.description || this.state.description.trim() === "") {
      this.setState({ error: "Description is required and cannot be empty" });
      return;
    }

    // Clear any previous errors
    this.setState({ error: "" });

    var data = {
      title: this.state.title.trim(),
      description: this.state.description.trim()
    };

    TutorialDataService.create(data)
      .then(response => {
        this.setState({
          id: response.data.id,
          title: response.data.title,
          description: response.data.description,
          published: response.data.published,
          submitted: true,
          error: "" // Clear error on success
        });
        console.log(response.data);
      })
      .catch(e => {
        console.log(e);
        // FIX ERR-011: Display error to user instead of silent failure
        this.setState({ error: "Failed to create tutorial. Please try again." });
      });
  }

  newTutorial() {
    this.setState({
      id: null,
      title: "",
      description: "",
      published: false,
      submitted: false,
      error: "" // Clear error when starting new tutorial
    });
  }

  render() {
    return (
      <div className="submit-form">
        {this.state.submitted ? (
          <div>
            <h4>You submitted successfully!</h4>
            <button className="btn btn-success" onClick={this.newTutorial}>
              Add
            </button>
          </div>
        ) : (
          <div>
            {/* FIX ERR-011: Display error message to user */}
            {this.state.error && (
              <div className="alert alert-danger" role="alert">
                {this.state.error}
              </div>
            )}

            <div className="form-group">
              <label htmlFor="title">Title</label>
              <input
                type="text"
                className="form-control"
                id="title"
                required
                value={this.state.title}
                onChange={this.onChangeTitle}
                name="title"
              />
            </div>

            <div className="form-group">
              <label htmlFor="description">Description</label>
              <input
                type="text"
                className="form-control"
                id="description"
                required
                value={this.state.description}
                onChange={this.onChangeDescription}
                name="description"
              />
            </div>

            <button onClick={this.saveTutorial} className="btn btn-success">
              Submit
            </button>
          </div>
        )}
      </div>
    );
  }
}
