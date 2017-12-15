package com.odde.mailsender.controller;

import com.odde.mailsender.service.AddressBookService;
import com.odde.mailsender.data.AddressItem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.validation.BeanPropertyBindingResult;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ContactListControllerTest {

    @MockBean
    private AddressBookService addressBookService;

    @Autowired
    private MockMvc mvc;

    @Test
    public void getSize1ContactList() throws Exception {
        List<AddressItem> result = new ArrayList<>();
        result.add(new AddressItem("aaa@example.com"));

        when(addressBookService.get()).thenReturn(result);
        mvc.perform(get("/contact-list"))
                .andExpect(model().attribute("contactList", hasSize(1)));
    }

    @Test
    public void addEmailAddress() throws Exception {
        performContactList("aaa@yahoo.co.jp", "aaa");

        verify(addressBookService).add(argThat(mail -> mail.getMailAddress().equals("aaa@yahoo.co.jp")));
        verify(addressBookService).add(argThat(mail -> mail.getName().equals("aaa")));
    }

    @Test
    public void addEmptyEmailAddress() throws Exception {
        assertAddressErrorMessage(performContactList(""), "{0} may not be empty");
    }

    @Test
    public void addNotValidEmailAddress() throws Exception {
        assertAddressErrorMessage(performContactList("aaa"), "Address format is wrong");
    }

    private MvcResult performContactList(String addressValue) throws Exception {
        return performContactList(addressValue, "aaa");
    }

    private MvcResult performContactList(String addressValue, String nameValue) throws Exception {
        return mvc.perform(post("/contact-list")
                .param("address", addressValue)
                .param("name", nameValue))
                .andExpect(view().name("contact-list")).andReturn();
    }

    private void assertAddressErrorMessage(MvcResult mvcResult, String expectMessage) {
        BeanPropertyBindingResult result = (BeanPropertyBindingResult)mvcResult.getModelAndView().getModelMap().get("org.springframework.validation.BindingResult.form");
        Assert.assertEquals(expectMessage, result.getFieldError("address").getDefaultMessage());
    }


    @Test
    public void createMailEmpty() throws Exception {
        mvc.perform(post("/create-mail"))
                .andExpect(view().name("send"))
                .andExpect(MockMvcResultMatchers.model().attribute("address",""));
    }

    @Test
    public void createMailTwo() throws Exception {
        mvc.perform(post("/create-mail").param("mailAddress", "aaa@yahoo.co.jp", "bbb@yahoo.co.jp"))
                .andExpect(view().name("send"))
                .andExpect(MockMvcResultMatchers.model().attribute("address","aaa@yahoo.co.jp;bbb@yahoo.co.jp"));
    }





}
