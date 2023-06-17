package com.hqh.client.controller;

import com.google.protobuf.Descriptors;
import com.hqh.client.service.BookAuthorClientService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
public class BookAuthorController {

    private final BookAuthorClientService bookAuthorClientService;

    @GetMapping("/author/{authorID}")
    public Map<Descriptors.FieldDescriptor, Object> getAuthor(@PathVariable String authorID) {
        return bookAuthorClientService.getAuthor(Integer.parseInt(authorID));
    }

    @GetMapping("/book/{authorID}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(@PathVariable String authorID)
            throws InterruptedException {
        return bookAuthorClientService.getBooksByAuthor(Integer.parseInt(authorID));
    }

    @GetMapping("/book")
    public Map<String, Map<Descriptors.FieldDescriptor, Object>> getExpensiveBook() throws InterruptedException {
        return bookAuthorClientService.getExpensiveBook();
    }

    @GetMapping("/book/author/{gender}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBookByGender(@PathVariable String gender) throws InterruptedException {
        return bookAuthorClientService.getBooksByGender(gender);
    }
}
