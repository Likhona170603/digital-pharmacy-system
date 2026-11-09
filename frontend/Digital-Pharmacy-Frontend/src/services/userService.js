import api, { qs, imgUrl } from './api';

export const normalizeStudent = (s) => ({
  id: s.studentId,
  role: 'STUDENT',
  fullName: s.fullName,
  name: s.fullName,
  email: s.email,
  phone: s.phone,
  bio: s.bio,
  course: s.course,
  courseId: s.courseId,
  studyFieldId: s.studyFieldId,
  studyField: s.studyField,
  residentialAreaId: s.residentialAreaId,
  residentialArea: s.residentialArea,
  streetAddress: s.streetAddress,
  needsStudyDetails: s.needsStudyDetails,
  institution: s.institution,
  location: s.location,
  avatar: imgUrl(s.profileImage),
  avatarRaw: s.profileImage,
  skills: s.skills || [],
  categories: s.categories || [],
  rating: s.averageRating,
  reviewCount: s.reviewCount,
});

export const normalizeClient = (c) => ({
  id: c.clientId,
  role: 'CLIENT',
  fullName: c.fullName,
  name: c.fullName,
  email: c.email,
  phone: c.phone,
  company: c.companyName,
  bio: c.bio,
  location: c.location,
  avatar: imgUrl(c.profileImage),
  avatarRaw: c.profileImage,
  rating: c.averageRating,
  reviewCount: c.reviewCount,
});

/** Students (login required). sort: 'rating' = highest rated first, otherwise newest first. Returns { items, page, size, totalItems, totalPages }. */
export const getStudents = async ({ q, categoryId, skillId, sort, page = 0, size = 12 } = {}) => {
  const data = await api(`/student${qs({ q, categoryId, skillId, sort, page, size })}`);
  return { ...data, items: data.items.map(normalizeStudent) };
};

export const getStudent = async (id) => normalizeStudent(await api(`/student/${id}`));
export const getClient = async (id) => normalizeClient(await api(`/client/${id}`));

/** Student and client ids equal their user id, so try student first, then client. */
export const getProfile = async (id) => {
  try {
    return await getStudent(id);
  } catch (e) {
    if (e.status === 404) return getClient(id);
    throw e;
  }
};

export const uploadProfileImage = async (file) => {
  const fd = new FormData();
  fd.append('file', file);
  return (await api('/upload/profile-image', { method: 'POST', body: fd })).url;
};

export const updateStudent = async (id, data) => normalizeStudent(await api(`/student/${id}`, { method: 'PUT', body: data }));
export const updateClient = async (id, data) => normalizeClient(await api(`/client/${id}`, { method: 'PUT', body: data }));
export const setStudentSkills = async (id, ids) => normalizeStudent(await api(`/student/${id}/skills`, { method: 'PUT', body: { ids } }));
export const setStudentCategories = async (id, ids) => normalizeStudent(await api(`/student/${id}/categories`, { method: 'PUT', body: { ids } }));
