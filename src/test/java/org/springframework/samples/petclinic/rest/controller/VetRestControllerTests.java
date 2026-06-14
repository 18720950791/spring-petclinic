/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for {@link VetRestController}
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@ContextConfiguration(classes=ApplicationTestConfig.class)
@WebAppConfiguration
class VetRestControllerTests {

    @Autowired
    private VetRestController vetRestController;

    @Autowired
    private VetMapper vetMapper;

	@MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    private List<Vet> vets;

    @BeforeEach
    void initVets(){
    	this.mockMvc = MockMvcBuilders.standaloneSetup(vetRestController)
    			.setControllerAdvice(new ExceptionControllerAdvice())
    			.build();
    	vets = new ArrayList<Vet>();

    	Specialty radiology = new Specialty();
    	radiology.setId(1);
    	radiology.setName("radiology");

    	Specialty surgery = new Specialty();
    	surgery.setId(2);
    	surgery.setName("surgery");

    	Specialty dentistry = new Specialty();
    	dentistry.setId(3);
    	dentistry.setName("dentistry");

    	Vet vet = new Vet();
    	vet.setId(1);
    	vet.setFirstName("James");
    	vet.setLastName("Carter");
    	vets.add(vet);

    	vet = new Vet();
    	vet.setId(2);
    	vet.setFirstName("Helen");
    	vet.setLastName("Leary");
    	vet.addSpecialty(radiology);
    	vets.add(vet);

    	vet = new Vet();
    	vet.setId(3);
    	vet.setFirstName("Linda");
    	vet.setLastName("Douglas");
    	vet.addSpecialty(surgery);
    	vet.addSpecialty(dentistry);
    	vets.add(vet);

    	Vet vet4 = new Vet();
    	vet4.setId(4);
    	vet4.setFirstName("Rafael");
    	vet4.setLastName("Ortega");
    	vet4.addSpecialty(surgery);
    	vets.add(vet4);

    	Vet vet5 = new Vet();
    	vet5.setId(5);
    	vet5.setFirstName("Henry");
    	vet5.setLastName("Stevens");
    	vet5.addSpecialty(radiology);
    	vets.add(vet5);
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetVetSuccess() throws Exception {
    	given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
        this.mockMvc.perform(get("/api/vets/1")
        	.accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetVetNotFound() throws Exception {
    	given(this.clinicService.findVetById(999)).willReturn(null);
        this.mockMvc.perform(get("/api/vets/999")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetAllVetsSuccess() throws Exception {
    	given(this.clinicService.findAllVets()).willReturn(vets);
        this.mockMvc.perform(get("/api/vets")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(1))
            .andExpect(jsonPath("$.[0].firstName").value("James"))
            .andExpect(jsonPath("$.[1].id").value(2))
            .andExpect(jsonPath("$.[1].firstName").value("Helen"));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetAllVetsNotFound() throws Exception {
    	vets.clear();
    	given(this.clinicService.findAllVets()).willReturn(vets);
        this.mockMvc.perform(get("/api/vets")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testCreateVetSuccess() throws Exception {
    	Vet newVet = vets.get(0);
    	newVet.setId(999);
    	ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	this.mockMvc.perform(post("/api/vets")
    		.content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
    		.andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testCreateVetError() throws Exception {
    	Vet newVet = vets.get(0);
    	newVet.setId(null);
    	newVet.setFirstName(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	this.mockMvc.perform(post("/api/vets")
        		.content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        		.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testUpdateVetSuccess() throws Exception {
    	given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
    	Vet newVet = vets.get(0);
    	newVet.setFirstName("James");
    	ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	this.mockMvc.perform(put("/api/vets/1")
    		.content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(content().contentType("application/json"))
        	.andExpect(status().isNoContent());

    	this.mockMvc.perform(get("/api/vets/1")
           	.accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("James"));

    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testUpdateVetError() throws Exception {
    	Vet newVet = vets.get(0);
    	newVet.setFirstName(null);
    	ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	this.mockMvc.perform(put("/api/vets/1")
    		.content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isBadRequest());
     }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testDeleteVetSuccess() throws Exception {
    	Vet newVet = vets.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	given(this.clinicService.findVetById(1)).willReturn(vets.get(0));
    	this.mockMvc.perform(delete("/api/vets/1")
    		.content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testDeleteVetError() throws Exception {
    	Vet newVet = vets.get(0);
    	ObjectMapper mapper = new ObjectMapper();
        String newVetAsJSON = mapper.writeValueAsString(vetMapper.toVetDto(newVet));
    	given(this.clinicService.findVetById(999)).willReturn(null);
    	this.mockMvc.perform(delete("/api/vets/999")
    		.content(newVetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
        	.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="OWNER_ADMIN")
    void testGetAllVetsForbidden() throws Exception {
    	this.mockMvc.perform(get("/api/vets")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetVetsByUnknownSpecialty() throws Exception {
    	given(this.clinicService.findVetsBySpecialtyNames(List.of("unknown"))).willReturn(new ArrayList<>());
    	this.mockMvc.perform(get("/api/vets")
        	.param("specialty", "unknown")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetVetsByMultipleSpecialties() throws Exception {
    	// vets matching radiology OR surgery: Helen(2), Linda(3), Rafael(4), Henry(5) — no duplicates
    	List<Vet> filtered = List.of(vets.get(1), vets.get(2), vets.get(3), vets.get(4));
    	given(this.clinicService.findVetsBySpecialtyNames(List.of("radiology", "surgery"))).willReturn(filtered);
    	this.mockMvc.perform(get("/api/vets")
        	.param("specialty", "radiology", "surgery")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[1].id").value(3))
            .andExpect(jsonPath("$.[2].id").value(4))
            .andExpect(jsonPath("$.[3].id").value(5));
    }

    @Test
    @WithMockUser(roles="VET_ADMIN")
    void testGetVetsBySpecialtyEmptyResult() throws Exception {
    	given(this.clinicService.findVetsBySpecialtyNames(List.of("dermatology"))).willReturn(new ArrayList<>());
    	this.mockMvc.perform(get("/api/vets")
        	.param("specialty", "dermatology")
        	.accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

}
