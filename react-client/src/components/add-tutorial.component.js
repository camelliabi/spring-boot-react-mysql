import React, { Component } from "react";
import TutorialDataService from "../services/tutorial.service";

export default class AddTutorial extends Component {
  constructor(props) {
    super(props);
    this.onChangeTitle = this.onChangeTitle.bind(this);
    this.onChangeDescription = this.onChangeDescription.bind(this);
    this.saveTutorial = this.saveTutorial.bind(this);
    this.newTutorial = this.newTutorial.bind(this);

    // FIX #7: Removed unused 'tags' property from state
    this.state = {
      id: null,
      title: "",
      description: "", 
      published: false,
      submitted: false
    };
  }

  // FIX #5: Removed trim() from onChange to allow users to type spaces
  // FIX #5: Trimming is now done only in saveTutorial before sending to server
  onChangeTitle(e) {
    const value = e.target.value;
    this.setState({
      title: value
    });
  }

  // FIX #5: Removed trim() from onChange
  onChangeDescription(e) {
    this.setState({
      description: e.target.value
    });
  }

  saveTutorial() {
    // FIX #5: Apply trim() here before sending to server
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
        
        // FIX #6: Removed direct state mutation (this.state.tags.push)
        // FIX #7: Removed tags functionality as it was unused
      })
      .catch(e => {
        console.log(e);
      });
  }

  newTutorial() {
    // FIX #7: Removed tags from state reset
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
