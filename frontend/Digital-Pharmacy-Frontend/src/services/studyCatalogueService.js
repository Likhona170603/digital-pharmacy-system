const API_ROOT = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/uniworks')
  .replace(/\/uniworks\/?$/, '');

async function getJson(path) {
  const response = await fetch(`${API_ROOT}${path}`);
  if (!response.ok) throw new Error('Could not load the CPUT study options. Please refresh the page.');
  return response.json();
}

export const getStudyFields = () => getJson('/catalogue/study-fields');
export const getCourses = (studyFieldId) =>
  studyFieldId ? getJson(`/catalogue/study-fields/${studyFieldId}/courses`) : Promise.resolve([]);
export const getResidentialAreas = () => getJson('/catalogue/residential-areas');
