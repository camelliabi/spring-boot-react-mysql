import http from "../http-common";

class TutorialDataService {
  getAll() {
    return http.get("/tutorials");
  }

  get(id) {
    return http.get(`/tutorials/${id}`);
  }

  create(data) {
    return http.post("/tutorials", data);
  }

  update(id, data) {
    return http.put(`/tutorials/${id}`, data);
  }

  delete(id) {
    return http.delete(`/tutorials/${id}`);
  }

  deleteAll() {
    return http.delete(`/tutorials`);
  }

  findByTitle(title) {
    // BUG #12 FIX: Use encodeURIComponent() to properly encode search parameter
    // This prevents issues with special characters like spaces, &, ?, #, etc.
    // Example: "React & Redux" becomes "React%20%26%20Redux"
    const encodedTitle = encodeURIComponent(title);
    return http.get(`/tutorials?title=${encodedTitle}`);
  }
}

export default new TutorialDataService();
