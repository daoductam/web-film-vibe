import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { HomePage } from './pages/HomePage';

import { SeriesPage } from './pages/series/SeriesPage';
import { SingleMoviePage } from './pages/movie/SingleMoviePage';
import { PopularPage } from './pages/popular/PopularPage';
import { MovieDetailPage } from './pages/movie/MovieDetailPage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/series" element={<SeriesPage />} />
        <Route path="/movies" element={<SingleMoviePage />} />
        <Route path="/popular" element={<PopularPage />} />
        <Route path="/movie/:slug" element={<MovieDetailPage />} />
      </Routes>
    </Router>
  );
}

export default App;
