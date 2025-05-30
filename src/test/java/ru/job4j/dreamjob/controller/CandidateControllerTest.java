package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.dreamjob.dto.FileDto;
import ru.job4j.dreamjob.model.Candidate;
import ru.job4j.dreamjob.model.City;
import ru.job4j.dreamjob.service.CandidateService;
import ru.job4j.dreamjob.service.CityService;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class CandidateControllerTest {

    private CandidateService candidateService;
    private CityService cityService;
    private CandidateController candidateController;
    private MultipartFile testFile;

    @BeforeEach
    public void initServices() {
        candidateService = mock(CandidateService.class);
        cityService = mock(CityService.class);
        candidateController = new CandidateController(candidateService, cityService);
        testFile = new MockMultipartFile("testFile.img", new byte[]{1, 2, 3});
    }

    @Test
    public void whenRequestCandidateListPageThenGetPageWithCandidates() {
        var candidate1 = new Candidate(1, "Candidate1", "Desc1", now(), 1, 2);
        var candidate2 = new Candidate(2, "Candidate2", "Desc2", now(), 3, 4);
        var expectedCandidates = List.of(candidate1, candidate2);
        when(candidateService.findAll()).thenReturn(expectedCandidates);

        var model = new ConcurrentModel();
        var view = candidateController.getAll(model);
        var actualCandidates = model.getAttribute("candidates");

        assertThat(view).isEqualTo("candidates/list");
        assertThat(actualCandidates).isEqualTo(expectedCandidates);
    }

    @Test
    public void whenRequestCandidateCreationPageThenGetPageWithCities() {
        var city1 = new City(1, "Москва");
        var city2 = new City(2, "Санкт-Петербург");
        var expectedCities = List.of(city1, city2);
        when(cityService.findAll()).thenReturn(expectedCities);

        var model = new ConcurrentModel();
        var view = candidateController.getCreationPage(model);
        var actualCities = model.getAttribute("cities");

        assertThat(view).isEqualTo("candidates/create");
        assertThat(actualCities).isEqualTo(expectedCities);
    }

    @Test
    public void whenPostCandidateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "Candidate1", "Desc1", now(), 1, 2);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.save(candidateArgumentCaptor.capture(),
                fileDtoArgumentCaptor.capture())).thenReturn(candidate);

        var model = new ConcurrentModel();
        var view = candidateController.create(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(fileDto).usingRecursiveComparison().isEqualTo(actualFileDto);
    }

    @Test
    public void whenSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Failed to write file");
        when(candidateService.save(any(), any())).thenThrow(expectedException);

        var model = new ConcurrentModel();
        var view = candidateController.create(new Candidate(), testFile, model);
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestCandidateByIdAndFoundThenGetPageWithCandidateAndCities() {
        var candidate = new Candidate(1, "Candidate1", "Desc1", now(), 1, 2);
        var cities = List.of(new City(1, "Москва"), new City(2, "Санкт-Петербург"));
        when(candidateService.findById(1)).thenReturn(Optional.of(candidate));
        when(cityService.findAll()).thenReturn(cities);

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 1);

        assertThat(view).isEqualTo("candidates/one");
        assertThat(model.getAttribute("candidate")).isEqualTo(candidate);
        assertThat(model.getAttribute("cities")).isEqualTo(cities);
    }

    @Test
    public void whenRequestCandidateByIdAndNotFoundThenGetErrorPage() {
        when(candidateService.findById(1)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = candidateController.getById(model, 1);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    public void whenPostCandidateUpdateWithFileThenSameDataAndRedirectToCandidatesPage() throws Exception {
        var candidate = new Candidate(1, "Candidate1", "Desc1", now(), 1, 2);
        var fileDto = new FileDto(testFile.getOriginalFilename(), testFile.getBytes());
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(),
                fileDtoArgumentCaptor.capture())).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);
        var actualCandidate = candidateArgumentCaptor.getValue();
        var actualFileDto = fileDtoArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/candidates");
        assertThat(actualCandidate).isEqualTo(candidate);
        assertThat(actualFileDto).usingRecursiveComparison().isEqualTo(fileDto);
    }

    @Test
    public void whenPostCandidateUpdateAndNotFoundThenGetErrorPage() {
        var candidate = new Candidate(1, "Candidate1", "Desc1", now(), 1, 2);
        var candidateArgumentCaptor = ArgumentCaptor.forClass(Candidate.class);
        var fileDtoArgumentCaptor = ArgumentCaptor.forClass(FileDto.class);
        when(candidateService.update(candidateArgumentCaptor.capture(),
                fileDtoArgumentCaptor.capture())).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.update(candidate, testFile, model);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo("Кандидат с указанным идентификатором не найден");
    }

    @Test
    public void whenRequestCandidateDeleteAndSuccessThenRedirectToCandidatesPage() {
        when(candidateService.deleteById(1)).thenReturn(true);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, 1);

        assertThat(view).isEqualTo("redirect:/candidates");
    }

    @Test
    public void whenRequestCandidateDeleteAndNotFoundThenGetErrorPage() {
        when(candidateService.deleteById(1)).thenReturn(false);

        var model = new ConcurrentModel();
        var view = candidateController.delete(model, 1);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message"))
                .isEqualTo("Кандидат с указанным идентификатором не найден");
    }
}