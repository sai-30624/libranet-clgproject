import { createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from '@/components/ProtectedRoute';

// Auth Pages
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';

// User Pages
import HomePage from '@/pages/user/HomePage';
import BookmarksPage from '@/pages/user/BookmarksPage';
import ProfilePage from '@/pages/user/ProfilePage';

// Admin Pages
import AdminDashboardPage from '@/pages/admin/AdminDashboardPage';
import AdminResourcesPage from '@/pages/admin/AdminResourcesPage';
import AdminUsersPage from '@/pages/admin/AdminUsersPage';
import AdminCategoriesPage from '@/pages/admin/AdminCategoriesPage';
import AdminFeedbackPage from '@/pages/admin/AdminFeedbackPage';

export const router = createBrowserRouter([
  // ── Public ─────────────────────────────────────────
  { path: '/login',          element: <LoginPage /> },
  { path: '/register',       element: <RegisterPage /> },

  // ── User (any authenticated) ────────────────────────
  {
    path: '/',
    element: <ProtectedRoute><HomePage /></ProtectedRoute>,
  },
  {
    path: '/bookmarks',
    element: <ProtectedRoute><BookmarksPage /></ProtectedRoute>,
  },
  {
    path: '/profile',
    element: <ProtectedRoute><ProfilePage /></ProtectedRoute>,
  },

  // ── Admin only ──────────────────────────────────────
  {
    path: '/admin',
    element: <ProtectedRoute role="ROLE_ADMIN"><AdminDashboardPage /></ProtectedRoute>,
  },
  {
    path: '/admin/resources',
    element: <ProtectedRoute role="ROLE_ADMIN"><AdminResourcesPage /></ProtectedRoute>,
  },
  {
    path: '/admin/users',
    element: <ProtectedRoute role="ROLE_ADMIN"><AdminUsersPage /></ProtectedRoute>,
  },
  {
    path: '/admin/categories',
    element: <ProtectedRoute role="ROLE_ADMIN"><AdminCategoriesPage /></ProtectedRoute>,
  },
  {
    path: '/admin/feedback',
    element: <ProtectedRoute role="ROLE_ADMIN"><AdminFeedbackPage /></ProtectedRoute>,
  },
]);
