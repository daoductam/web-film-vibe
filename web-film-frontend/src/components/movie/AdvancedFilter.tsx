import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { taxonomyService } from '../../services/taxonomy.service';
import { Filter, ChevronDown, X } from 'lucide-react';

interface AdvancedFilterProps {
    onFilterChange: (filters: { categorySlug?: string; countrySlug?: string; year?: string }) => void;
}

export const AdvancedFilter = ({ onFilterChange }: AdvancedFilterProps) => {
    const [selectedCategory, setSelectedCategory] = useState<string>('');
    const [selectedCountry, setSelectedCountry] = useState<string>('');
    const [selectedYear, setSelectedYear] = useState<string>('');
    const [isOpen, setIsOpen] = useState(false);

    const { data: categories } = useQuery({
        queryKey: ['categories'],
        queryFn: taxonomyService.getCategories
    });

    const { data: countries } = useQuery({
        queryKey: ['countries'],
        queryFn: taxonomyService.getCountries
    });

    const currentYear = new Date().getFullYear();
    const years = Array.from({ length: 15 }, (_, i) => (currentYear - i).toString());

    const handleApply = () => {
        onFilterChange({
            categorySlug: selectedCategory || undefined,
            countrySlug: selectedCountry || undefined,
            year: selectedYear || undefined,
        });
        setIsOpen(false);
    };

    const handleReset = () => {
        setSelectedCategory('');
        setSelectedCountry('');
        setSelectedYear('');
        onFilterChange({});
        setIsOpen(false);
    };

    return (
        <div className="relative">
            <button 
                onClick={() => setIsOpen(!isOpen)}
                className="flex items-center gap-2 px-4 py-2 bg-white/5 border border-white/10 rounded-xl text-white hover:bg-white/10 transition-colors"
            >
                <Filter className="w-4 h-4" />
                <span className="text-sm font-medium">Bộ lọc</span>
                <ChevronDown className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
            </button>

            {isOpen && (
                <div className="absolute top-full left-0 mt-3 w-[320px] sm:w-[450px] p-6 bg-obsidian border border-white/10 rounded-2xl shadow-[0_20px_50px_rgba(0,0,0,0.5)] z-[100] animate-in fade-in zoom-in-95 duration-200">
                    <div className="flex items-center justify-between mb-6">
                        <h3 className="text-lg font-bold text-white">Bộ lọc nâng cao</h3>
                        <button onClick={() => setIsOpen(false)} className="text-gray-500 hover:text-white"><X size={20} /></button>
                    </div>
                    <div className="grid grid-cols-2 gap-4 mb-4">
                        {/* Category */}
                        <div>
                            <label className="block text-xs font-medium text-gray-400 mb-1">Thể loại</label>
                            <select 
                                value={selectedCategory}
                                onChange={(e) => setSelectedCategory(e.target.value)}
                                className="w-full bg-black/50 border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-neon"
                            >
                                <option value="">Tất cả</option>
                                {categories?.map(c => (
                                    <option key={c.id} value={c.slug}>{c.name}</option>
                                ))}
                            </select>
                        </div>
                        {/* Country */}
                        <div>
                            <label className="block text-xs font-medium text-gray-400 mb-1">Quốc gia</label>
                            <select 
                                value={selectedCountry}
                                onChange={(e) => setSelectedCountry(e.target.value)}
                                className="w-full bg-black/50 border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:border-neon"
                            >
                                <option value="">Tất cả</option>
                                {countries?.map(c => (
                                    <option key={c.id} value={c.slug}>{c.name}</option>
                                ))}
                            </select>
                        </div>
                        {/* Year */}
                        <div className="col-span-2">
                            <label className="block text-xs font-medium text-gray-400 mb-1">Năm phát hành</label>
                            <div className="flex flex-wrap gap-2">
                                <button
                                    onClick={() => setSelectedYear('')}
                                    className={`px-3 py-1 rounded-full text-xs transition-colors ${!selectedYear ? 'bg-neon text-obsidian' : 'bg-white/5 text-gray-300 hover:bg-white/10'}`}
                                >
                                    Tất cả
                                </button>
                                {years.map(y => (
                                    <button
                                        key={y}
                                        onClick={() => setSelectedYear(y)}
                                        className={`px-3 py-1 rounded-full text-xs transition-colors ${selectedYear === y ? 'bg-neon text-obsidian' : 'bg-white/5 text-gray-300 hover:bg-white/10'}`}
                                    >
                                        {y}
                                    </button>
                                ))}
                            </div>
                        </div>
                    </div>
                    
                    <div className="flex gap-2">
                        <button 
                            onClick={handleReset}
                            className="flex-1 py-2 rounded-lg border border-white/10 text-gray-300 hover:bg-white/5 transition-colors text-sm font-medium"
                        >
                            Đặt lại
                        </button>
                        <button 
                            onClick={handleApply}
                            className="flex-1 py-2 rounded-lg bg-neon text-obsidian font-bold hover:bg-white transition-colors text-sm"
                        >
                            Áp dụng
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};
