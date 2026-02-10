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
      // FIX #7: Removed unused 'tags' array from state
      // BEFORE: tags: []
      // ISSUE: The tags array was declared but never actually used in the component
      //        It was only mutated directly with this.state.tags.push() which violates React principles
      // SOLUTION: Removed the unused property entirely
      // IMPACT: Cleaner component state, reduced memory footprint
    };
  }

  onChangeTitle(e) {
    // FIX #6: Removed trim() from input handler
    // BEFORE: this.setState({ title: e.target.value.trim() });
    // ISSUE: Calling trim() on every keystroke removed leading/trailing spaces immediately
    //        - User types " Hello" → trim() makes it "Hello" → cursor position breaks
    //        - Users cannot start with spaces or have trailing spaces while typing
    //        - Causes poor UX as input value changes unexpectedly during typing
    // SOLUTION: Store the raw value; trim only when saving (see saveTutorial method)
    // IMPACT: Natural typing experience; validation happens at submit time, not during input
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
    // FIX #6 (continued): Trim values when saving, not during input
    // This is the proper place to sanitize user input - at submission time
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
          submitted: true
        });
        console.log(response.data);
        
        // FIX #7: Removed direct state mutation
        // BEFORE: this.state.tags.push("new-tutorial");
        // ISSUE: Direct mutation of state array using this.state.tags.push()
        //        - Bypasses React's state management system
        //        - Violates React's core principle: never mutate state directly
        //        - Can cause rendering bugs and unpredictable behavior
        //        - React may not detect the change, skipping re-renders
        // SOLUTION: Removed this line since tags array is unused
        //           If needed in future, proper solution would be:
        //           this.setState({ tags: [...this.state.tags, "new-tutorial"] })
        // IMPACT: Prevents React reconciliation bugs, follows React best practices
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
