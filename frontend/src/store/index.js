import { createStore } from 'vuex'

export default createStore({
  state: {
    analysisResults: null,
    loading: false,
    error: null
  },
  mutations: {
    setResults(state, results) {
      state.analysisResults = results;
    },
    setLoading(state, status) {
      state.loading = status;
    },
    setError(state, error) {
      state.error = error;
    }
  },
  actions: {
    updateResults({ commit }, results) {
      commit('setResults', results);
    },
    startLoading({ commit }) {
      commit('setLoading', true);
    },
    stopLoading({ commit }) {
      commit('setLoading', false);
    },
    setError({ commit }, error) {
      commit('setError', error);
    }
  },
  getters: {
    getResults: state => state.analysisResults,
    isLoading: state => state.loading,
    getError: state => state.error
  }
}) 