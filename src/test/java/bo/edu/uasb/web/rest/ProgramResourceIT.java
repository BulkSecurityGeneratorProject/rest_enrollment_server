package bo.edu.uasb.web.rest;

import bo.edu.uasb.EnrollmentApp;
import bo.edu.uasb.domain.Program;
import bo.edu.uasb.repository.ProgramRepository;
import bo.edu.uasb.service.ProgramService;
import bo.edu.uasb.web.rest.errors.ExceptionTranslator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import javax.persistence.EntityManager;
import java.util.List;

import static bo.edu.uasb.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import bo.edu.uasb.domain.enumeration.Semesters;
/**
 * Integration tests for the {@Link ProgramResource} REST controller.
 */
@SpringBootTest(classes = EnrollmentApp.class)
public class ProgramResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Semesters DEFAULT_SEMESTERS = Semesters.FIRST;
    private static final Semesters UPDATED_SEMESTERS = Semesters.SECOND;

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final Integer DEFAULT_CREDITS = 1;
    private static final Integer UPDATED_CREDITS = 2;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramService programService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    @Autowired
    private Validator validator;

    private MockMvc restProgramMockMvc;

    private Program program;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ProgramResource programResource = new ProgramResource(programService);
        this.restProgramMockMvc = MockMvcBuilders.standaloneSetup(programResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter)
            .setValidator(validator).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Program createEntity(EntityManager em) {
        Program program = new Program()
            .name(DEFAULT_NAME)
            .semesters(DEFAULT_SEMESTERS)
            .title(DEFAULT_TITLE)
            .credits(DEFAULT_CREDITS);
        return program;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Program createUpdatedEntity(EntityManager em) {
        Program program = new Program()
            .name(UPDATED_NAME)
            .semesters(UPDATED_SEMESTERS)
            .title(UPDATED_TITLE)
            .credits(UPDATED_CREDITS);
        return program;
    }

    @BeforeEach
    public void initTest() {
        program = createEntity(em);
    }

    @Test
    @Transactional
    public void createProgram() throws Exception {
        int databaseSizeBeforeCreate = programRepository.findAll().size();

        // Create the Program
        restProgramMockMvc.perform(post("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(program)))
            .andExpect(status().isCreated());

        // Validate the Program in the database
        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeCreate + 1);
        Program testProgram = programList.get(programList.size() - 1);
        assertThat(testProgram.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testProgram.getSemesters()).isEqualTo(DEFAULT_SEMESTERS);
        assertThat(testProgram.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testProgram.getCredits()).isEqualTo(DEFAULT_CREDITS);
    }

    @Test
    @Transactional
    public void createProgramWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = programRepository.findAll().size();

        // Create the Program with an existing ID
        program.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restProgramMockMvc.perform(post("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(program)))
            .andExpect(status().isBadRequest());

        // Validate the Program in the database
        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = programRepository.findAll().size();
        // set the field null
        program.setName(null);

        // Create the Program, which fails.

        restProgramMockMvc.perform(post("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(program)))
            .andExpect(status().isBadRequest());

        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkSemestersIsRequired() throws Exception {
        int databaseSizeBeforeTest = programRepository.findAll().size();
        // set the field null
        program.setSemesters(null);

        // Create the Program, which fails.

        restProgramMockMvc.perform(post("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(program)))
            .andExpect(status().isBadRequest());

        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = programRepository.findAll().size();
        // set the field null
        program.setTitle(null);

        // Create the Program, which fails.

        restProgramMockMvc.perform(post("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(program)))
            .andExpect(status().isBadRequest());

        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkCreditsIsRequired() throws Exception {
        int databaseSizeBeforeTest = programRepository.findAll().size();
        // set the field null
        program.setCredits(null);

        // Create the Program, which fails.

        restProgramMockMvc.perform(post("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(program)))
            .andExpect(status().isBadRequest());

        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPrograms() throws Exception {
        // Initialize the database
        programRepository.saveAndFlush(program);

        // Get all the programList
        restProgramMockMvc.perform(get("/api/programs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(program.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())))
            .andExpect(jsonPath("$.[*].semesters").value(hasItem(DEFAULT_SEMESTERS.toString())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].credits").value(hasItem(DEFAULT_CREDITS)));
    }
    
    @Test
    @Transactional
    public void getProgram() throws Exception {
        // Initialize the database
        programRepository.saveAndFlush(program);

        // Get the program
        restProgramMockMvc.perform(get("/api/programs/{id}", program.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(program.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.semesters").value(DEFAULT_SEMESTERS.toString()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.credits").value(DEFAULT_CREDITS));
    }

    @Test
    @Transactional
    public void getNonExistingProgram() throws Exception {
        // Get the program
        restProgramMockMvc.perform(get("/api/programs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateProgram() throws Exception {
        // Initialize the database
        programService.save(program);

        int databaseSizeBeforeUpdate = programRepository.findAll().size();

        // Update the program
        Program updatedProgram = programRepository.findById(program.getId()).get();
        // Disconnect from session so that the updates on updatedProgram are not directly saved in db
        em.detach(updatedProgram);
        updatedProgram
            .name(UPDATED_NAME)
            .semesters(UPDATED_SEMESTERS)
            .title(UPDATED_TITLE)
            .credits(UPDATED_CREDITS);

        restProgramMockMvc.perform(put("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedProgram)))
            .andExpect(status().isOk());

        // Validate the Program in the database
        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeUpdate);
        Program testProgram = programList.get(programList.size() - 1);
        assertThat(testProgram.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testProgram.getSemesters()).isEqualTo(UPDATED_SEMESTERS);
        assertThat(testProgram.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testProgram.getCredits()).isEqualTo(UPDATED_CREDITS);
    }

    @Test
    @Transactional
    public void updateNonExistingProgram() throws Exception {
        int databaseSizeBeforeUpdate = programRepository.findAll().size();

        // Create the Program

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restProgramMockMvc.perform(put("/api/programs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(program)))
            .andExpect(status().isBadRequest());

        // Validate the Program in the database
        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteProgram() throws Exception {
        // Initialize the database
        programService.save(program);

        int databaseSizeBeforeDelete = programRepository.findAll().size();

        // Delete the program
        restProgramMockMvc.perform(delete("/api/programs/{id}", program.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Program> programList = programRepository.findAll();
        assertThat(programList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Program.class);
        Program program1 = new Program();
        program1.setId(1L);
        Program program2 = new Program();
        program2.setId(program1.getId());
        assertThat(program1).isEqualTo(program2);
        program2.setId(2L);
        assertThat(program1).isNotEqualTo(program2);
        program1.setId(null);
        assertThat(program1).isNotEqualTo(program2);
    }
}
