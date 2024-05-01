package ru.kim.partynote.compilation.service;

import ru.kim.partynote.compilation.dto.CompilationDto;
import ru.kim.partynote.compilation.dto.NewCompilationDto;
import ru.kim.partynote.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto getPublicCompilationById(Long compId);

    List<CompilationDto> getPublicCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto saveCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilation);
}
