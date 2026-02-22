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
      submitted: false
      // FIX #9: Removed unused 'tags' state property
      // ORIGINAL BUG: 'tags' array was initialized but never read or used properly
      // It was only mutated incorrectly (direct state mutation)
      // FIXED: Removed dead code to improve maintainability
    };
  }

  onChangeTitle(e) {
    const value = e.target.value;
    // FIX #5: Fixed input trimming on keystroke
    // ORIGINAL BUG: Used value.trim() which prevented typing spaces during input
    // Users couldn't type multi-word titles naturally (e.g., "My Tutorial")
    // FIXED: Store raw value, trim only on submit in saveTutorial()
    this.setState({
      title: value
    });
  }

  onChangeDescription(e) {
    this.setState({
      description: e.target.value
    });
  }

  saveTutorial() {
    // Trim whitespace only when submitting, not during typing
    var data = {
      title: this.state.title.trim(),
      description: this.state.description
    };

    TutorialDataService.create(data)
      .then(response => {
        this.setState({
          id: response.data.id,
          title: response.data.title,
          description: response.data.description,
          published: response.data.published,
          submitted: true
        });
        console.log(response.data);
        
        // FIX #6: Removed direct state mutation
        // ORIGINAL BUG: this.state.tags.push("new-tutorial") directly mutated state
        // This violates React's immutability contract and prevents re-renders
        // FIXED: Removed entirely as 'tags' was unused (see FIX #9)
      })
      .catch(e => {
        console.log(e);
      });
  }

  newTutorial() {
    this.setState({
      id: null,
      title: "",
      description: "",
      published: false,
      submitted: false
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
