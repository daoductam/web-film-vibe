import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';

export const RegisterPage = () => {
    const [showPassword, setShowPassword] = useState(false);
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleRegister = (e: React.FormEvent) => {
        e.preventDefault();
        console.log('Register with:', { name, email, password });
        // For demo purposes, go to login after "registering"
        navigate('/login');
    };

    return (
        <div className="relative w-full h-screen flex items-center justify-center overflow-hidden bg-obsidian text-text-primary font-sans antialiased selection:bg-neon selection:text-obsidian">
            {/* Background */}
            <div className="absolute inset-0 z-0">
                <div className="absolute inset-0 bg-obsidian/80 z-10"></div>
                <div 
                    className="w-full h-full bg-cover bg-center" 
                    style={{ backgroundImage: 'url("https://lh3.googleusercontent.com/aida-public/AB6AXuCR-i8dtBbQiiS4Fb598zX8V6j0GXygXiDa_gEJxZ_ohvvFQzxQcMLJv5SYo8HSfgsPQA4SaWAWk2v7Y6nZT6dcwLIjdxgXsdcW1BJTSxfNBLQYhn1MbUlcFwIM5h00YjHADRo-hh54ZtBlyGHhwcI-3bk7X8xKWbsWIBEqN58PpCddpvDwkV4zADrUOD-PoLuKbYC5Ocw-8VGjCvjsqYdGFYE0oK-H1T_jKqNYk8-M7Ub-yCnK7Kn7Y77WYxAj1s1E62TpqfW_xFU")' }}
                ></div>
            </div>

            {/* Register Card */}
            <div className="relative z-20 w-full max-w-md px-6 my-8">
                <div className="bg-[#191923]/60 backdrop-blur-xl border border-[#00f3ff]/30 rounded-3xl p-8 md:p-10 shadow-[0_8px_32px_0_rgba(0,0,0,0.37)] animate-[fadeInUp_0.8s_ease-out_forwards] shadow-[0_0_15px_rgba(0,243,255,0.15),inset_0_0_5px_rgba(0,243,255,0.05)]">
                    
                    {/* Header */}
                    <div className="flex flex-col items-center mb-8">
                        <Link to="/" className="flex items-center gap-2 mb-2 group cursor-pointer hover:opacity-80 transition-opacity">
                            <div className="size-8 text-neon drop-shadow-[0_0_8px_rgba(0,243,255,0.6)]">
                                <svg fill="currentColor" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">
                                    <path clipRule="evenodd" d="M39.475 21.6262C40.358 21.4363 40.6863 21.5589 40.7581 21.5934C40.7876 21.655 40.8547 21.857 40.8082 22.3336C40.7408 23.0255 40.4502 24.0046 39.8572 25.2301C38.6799 27.6631 36.5085 30.6631 33.5858 33.5858C30.6631 36.5085 27.6632 38.6799 25.2301 39.8572C24.0046 40.4502 23.0255 40.7407 22.3336 40.8082C21.8571 40.8547 21.6551 40.7875 21.5934 40.7581C21.5589 40.6863 21.4363 40.358 21.6262 39.475C21.8562 38.4054 22.4689 36.9657 23.5038 35.2817C24.7575 33.2417 26.5497 30.9744 28.7621 28.762C30.9744 26.5497 33.2417 24.7574 35.2817 23.5037C36.9657 22.4689 38.4054 21.8562 39.475 21.6262ZM4.41189 29.2403L18.7597 43.5881C19.8813 44.7097 21.4027 44.9179 22.7217 44.7893C24.0585 44.659 25.5148 44.1631 26.9723 43.4579C29.9052 42.0387 33.2618 39.5667 36.4142 36.4142C39.5667 33.2618 42.0387 29.9052 43.4579 26.9723C44.1631 25.5148 44.659 24.0585 44.7893 22.7217C44.9179 21.4027 44.7097 19.8813 43.5881 18.7597L29.2403 4.41187C27.8527 3.02428 25.8765 3.02573 24.2861 3.36776C22.6081 3.72863 20.7334 4.58419 18.8396 5.74801C16.4978 7.18716 13.9881 9.18353 11.5858 11.5858C9.18354 13.988 7.18717 16.4978 5.74802 18.8396C4.58421 20.7334 3.72865 22.6081 3.36778 24.2861C3.02574 25.8765 3.02429 27.8527 4.41189 29.2403Z" fill="currentColor" fillRule="evenodd"></path>
                                </svg>
                            </div>
                            <h1 className="text-white text-2xl font-serif font-black tracking-tight">Cine<span className="text-neon">Stream</span></h1>
                        </Link>
                        <h2 className="text-3xl font-serif font-bold text-white mt-2">Đăng ký</h2>
                        <p className="text-text-secondary text-sm mt-2">Bắt đầu hành trình điện ảnh của bạn ngay hôm nay</p>
                    </div>

                    {/* Form */}
                    <form className="space-y-6" onSubmit={handleRegister}>
                        <div className="space-y-4">
                            <div className="relative group">
                                <input 
                                    className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm placeholder-gray-500 transition-all peer focus:bg-white/10 focus:border-neon focus:shadow-[0_0_10px_rgba(0,243,255,0.2)] outline-none" 
                                    id="name" 
                                    placeholder=" " 
                                    type="text"
                                    value={name}
                                    onChange={(e) => setName(e.target.value)}
                                    required
                                />
                                <label 
                                    className="absolute left-4 top-3 text-gray-400 text-sm transition-all duration-200 peer-focus:-translate-y-6 peer-focus:text-xs peer-focus:text-neon peer-[:not(:placeholder-shown)]:-translate-y-6 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:text-neon pointer-events-none bg-transparent" 
                                    htmlFor="name"
                                >
                                    Họ và tên
                                </label>
                            </div>
                            <div className="relative group">
                                <input 
                                    className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm placeholder-gray-500 transition-all peer focus:bg-white/10 focus:border-neon focus:shadow-[0_0_10px_rgba(0,243,255,0.2)] outline-none" 
                                    id="email" 
                                    placeholder=" " 
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                                <label 
                                    className="absolute left-4 top-3 text-gray-400 text-sm transition-all duration-200 peer-focus:-translate-y-6 peer-focus:text-xs peer-focus:text-neon peer-[:not(:placeholder-shown)]:-translate-y-6 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:text-neon pointer-events-none bg-transparent" 
                                    htmlFor="email"
                                >
                                    Địa chỉ Email
                                </label>
                            </div>
                            <div className="relative group">
                                <input 
                                    className="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm placeholder-gray-500 transition-all peer focus:bg-white/10 focus:border-neon focus:shadow-[0_0_10px_rgba(0,243,255,0.2)] outline-none" 
                                    id="password" 
                                    placeholder=" " 
                                    type={showPassword ? "text" : "password"}
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                                <label 
                                    className="absolute left-4 top-3 text-gray-400 text-sm transition-all duration-200 peer-focus:-translate-y-6 peer-focus:text-xs peer-focus:text-neon peer-[:not(:placeholder-shown)]:-translate-y-6 peer-[:not(:placeholder-shown)]:text-xs peer-[:not(:placeholder-shown)]:text-neon pointer-events-none bg-transparent" 
                                    htmlFor="password"
                                >
                                    Mật khẩu
                                </label>
                                <button 
                                    className="absolute right-3 top-3 text-gray-500 hover:text-white transition-colors" 
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                >
                                    <span className="material-symbols-outlined text-lg">
                                        {showPassword ? 'visibility_off' : 'visibility'}
                                    </span>
                                </button>
                            </div>
                        </div>
                        
                        <div className="text-xs text-text-secondary leading-relaxed">
                            Bằng cách đăng ký, bạn đồng ý với <a href="#" className="text-white hover:text-neon transition-colors font-medium">Điều khoản sử dụng</a> và <a href="#" className="text-white hover:text-neon transition-colors font-medium">Chính sách bảo mật</a> của chúng tôi.
                        </div>

                        <button 
                            className="w-full py-3.5 rounded-xl bg-gradient-to-r from-neon to-[#00c2cc] hover:to-neon text-obsidian font-bold text-base transition-all shadow-[0_0_20px_rgba(0,243,255,0.3)] hover:shadow-[0_0_30px_rgba(0,243,255,0.5)] transform hover:-translate-y-0.5" 
                            type="submit"
                        >
                            Đăng ký tài khoản
                        </button>
                    </form>

                    {/* Divider */}
                    <div className="relative my-8">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-white/10"></div>
                        </div>
                        <div className="relative flex justify-center text-xs uppercase">
                            <span className="bg-[#131720] px-2 text-text-secondary rounded">Hoặc tiếp tục với</span>
                        </div>
                    </div>

                    {/* Social Login */}
                    <div className="flex gap-4">
                        <button className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 hover:border-white/30 transition-all group">
                            <img alt="Google" className="w-5 h-5" src="https://lh3.googleusercontent.com/aida-public/AB6AXuAaER3NXezcCP8a5cbI1c-aqzm9cG-Wn6gZqSc4N9aUkgwO7a-l7jEkjTNgiWoTYqpAe1VsQ8H-coQT7tp-MTSSSNOAlFCwj3f7yOtSUJNnm8ld0VY8_Vat8XHvMVpmM_V82Zl4DEjQlUbx1217mTk6l1FVProRWom4AfjIULO4-OURWPN8HVYqTGCwPG_WMwJ-2z2WzGduaibACPylCBURRyaBp5Jbusdb_GNl2uHN223ss00KPrKUtryWLZ8JI9zJ10A4eK94AFU"/>
                            <span className="text-white text-sm font-medium group-hover:text-white">Google</span>
                        </button>
                        <button className="flex-1 flex items-center justify-center gap-2 py-2.5 rounded-xl bg-white/5 hover:bg-white/10 border border-white/10 hover:border-white/30 transition-all group">
                            <img alt="Facebook" className="w-5 h-5" src="https://lh3.googleusercontent.com/aida-public/AB6AXuC6n2unzuAB8z-7WEJQAQPN9HY2yoLD7_klIgLQX4dOLfSq-0FEeQQ4198qKhAVL6dcWffx7MGkSuyc_ayWWHXU23oiaxryJ1LkezLi9T8qhkK_huuPvHsJrWDFVUz8QhexXTYXNb3_WhI-F95M_E_ZXF3rw3G3jbNtEzHNQWoSLi5IZGixLTOIYdIBbJnjbIseoujGNeZaFqwxbidcUeM3pWt3ix0MLazsU7CLIiNpJni5GdbURIfZ9vknrdhmaG5mAsMdpftI6tI"/>
                            <span className="text-white text-sm font-medium group-hover:text-white">Facebook</span>
                        </button>
                    </div>

                    {/* Footer */}
                    <div className="mt-8 text-center">
                        <p className="text-text-secondary text-sm">
                            Đã có tài khoản? 
                            <Link to="/login" className="text-white font-bold hover:text-neon transition-colors ml-1">Đăng nhập ngay</Link>
                        </p>
                    </div>
                </div>

                {/* Bottom Links */}
                <div className="text-center mt-6 opacity-60 hover:opacity-100 transition-opacity duration-300">
                    <a className="text-xs text-text-secondary hover:text-neon transition-colors mx-2" href="#">Trung tâm hỗ trợ</a>
                    <span className="text-text-secondary text-xs">•</span>
                    <a className="text-xs text-text-secondary hover:text-neon transition-colors mx-2" href="#">Điều khoản sử dụng</a>
                    <span className="text-text-secondary text-xs">•</span>
                    <a className="text-xs text-text-secondary hover:text-neon transition-colors mx-2" href="#">Quyền riêng tư</a>
                </div>
            </div>
        </div>
    );
};
