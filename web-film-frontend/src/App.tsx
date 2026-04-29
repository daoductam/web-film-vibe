// CI/CD Deployment Trigger Comment
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { HomePage } from './pages/HomePage';

import { SeriesPage } from './pages/series/SeriesPage';
import { SingleMoviePage } from './pages/movie/SingleMoviePage';
import { PopularPage } from './pages/popular/PopularPage';
import { MovieDetailPage } from './pages/movie/MovieDetailPage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { AIChatWidget } from './components/ai/AIChatWidget';
import { AdminLayout } from './components/layout/AdminLayout';
import { DashboardPage } from './pages/admin/DashboardPage';
import { CrawlPage } from './pages/admin/CrawlPage';
import { SearchPage } from './pages/search/SearchPage';
import { ToastProvider } from './components/common/Toast';

function App() {
  return (
    <ToastProvider>
      <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/series" element={<SeriesPage />} />
        <Route path="/movies" element={<SingleMoviePage />} />
        <Route path="/popular" element={<PopularPage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/movie/:slug" element={<MovieDetailPage />} />
        
        {/* Admin Routes */}
        <Route path="/admin" element={<AdminLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="crawl" element={<CrawlPage />} />
          {/* Add more admin routes here as needed */}
        </Route>
      </Routes>
      <AIChatWidget />
    </Router>
    </ToastProvider>
  );
}

export default App;
