package com.github.raftimpl.raft.controller;

import com.github.raftimpl.raft.template.RaftTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/raft")
public class TestController {
    @Autowired
    private RaftTemplate raftTemplate;

    @PostMapping(value = "/write", produces = "application/json;charset=UTF-8")
    public String write(@RequestParam String key, @RequestParam String value) {
        return raftTemplate.write(key, value);
    }

    @GetMapping(value = "/read", produces = "application/json;charset=UTF-8")
    public String read(@RequestParam String key) {
        return raftTemplate.read(key);
    }
}
