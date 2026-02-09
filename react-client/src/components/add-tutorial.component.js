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

      tags: []
    };
  }

  onChangeTitle(e) {
    // FIX #7: Removed trim() that caused input lag and prevented spaces
    // ISSUE: Calling trim() on every keystroke removed leading/trailing spaces immediately
    // ORIGINAL CODE: title: value.trim()
    // PROBLEM:
    //   - User types " Hello" → trim() makes it "Hello" → cursor position breaks
    //   - User can't start with spaces or have trailing spaces while typing
    //   - Causes poor UX as input value changes unexpectedly during typing
    // SOLUTION: Store the raw value; trim only when saving if needed
    // IMPACT: Natural typing experience; validation happens at submit time, not during input
    const value = e.target.value;

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
    // Trim title before saving to remove accidental whitespace
    var data = {
      title: this.state.title.trim(),
      description: this.state.description
    };


    
    TutorialDataService.create(data)
      .then(response => {
        // FIX #8: Replaced direct state mutation with proper setState for tags
        // ISSUE: this.state.tags.push("new-tutorial") directly mutates state
        // ORIGINAL CODE: this.state.tags.push("new-tutorial");
        // PROBLEM:
        //   - Violates React's principle: never mutate state directly
        //   - Can cause unpredictable rendering behavior
        //   - React may not detect the change, skipping re-renders
        //   - Creates hard-to-debug issues in complex components
        // SOLUTION: Use setState with spread operator to create new array
        // IMPACT: Follows React best practices; ensures reliable state updates
        this.setState({
          id: response.data.id,
          title: response.data.title,
          description: response.data.description,
          published: response.data.published,
          submitted: true,
          tags: [...this.state.tags, "new-tutorial"]
        });
        console.log(response.data);
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
      submitted: false,
      tags: []
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
