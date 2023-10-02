package com.example.pdfreader

import android.graphics.*
import java.util.*

// current page
var current = 0

var curTool = "hand"
var Ops: MutableList<Op> = mutableListOf()
var undo: Stack<Op> = Stack()
var redo: Stack<Op> = Stack()

class Op(var path: Path, var tool: String, var page: Int, var isVisible: Boolean)