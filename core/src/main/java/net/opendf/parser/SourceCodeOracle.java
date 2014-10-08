package net.opendf.parser;

import java.io.File;

import net.opendf.ir.IRNode;
import net.opendf.ir.IRNode.Identifier;

import beaver.Symbol;

public interface SourceCodeOracle {
	public SourceCodePosition getSrcLocations(Identifier id);
	public void register(IRNode node, SourceCodePosition pos);

	public class SourceCodePosition {
		private SourceCodePosition(){ }

		/**
		 * Factory
		 */
		public static SourceCodePosition newIncluding(Symbol start, Symbol end, File file){
			SourceCodePosition pos = new SourceCodePosition();
			pos.file = file;
			pos.start = start.getStart();
			pos.end = end.getEnd();
			return pos;
		}

		public static SourceCodePosition newIncluding(SourceCodePosition start, Symbol end){
			SourceCodePosition pos = new SourceCodePosition();
			if(start != null){  // failed source info lookup
				pos.file = start.file;
				pos.start = start.start;
			}
			pos.end = end.getEnd();
			return pos;
		}

		public static SourceCodePosition newIncluding(Symbol start, SourceCodePosition end){
			SourceCodePosition pos = new SourceCodePosition();
			pos.start = start.getStart();
			if(end != null){  // failed source info lookup
				pos.file = end.file;
				pos.end = end.end;
			}
			return pos;
		}
		
		public static SourceCodePosition newIncluding(SourceCodePosition first, SourceCodePosition last){
			SourceCodePosition pos = new SourceCodePosition();
			if(first != null){
				pos.start = first.start;
				pos.file = first.file;
			}
			if(last != null){
				pos.end = last.end;
				pos.file = last.file;
			}
			return pos;
		}

		public static SourceCodePosition newBetween(SourceCodePosition before, SourceCodePosition after){
			SourceCodePosition pos = new SourceCodePosition();
			if(before != null){
				pos.start = before.end + 1;
				pos.file = before.file;
			}
			if(after != null){
				pos.end = after.start;
				if(after.getStartColumn() > 0){
					pos.end -= 1;
				}
				pos.file = after.file;
			}
			return pos;
		}

		public static SourceCodePosition newExcludeEnd(SourceCodePosition first, SourceCodePosition end){
			SourceCodePosition pos = new SourceCodePosition();
			if(first != null){
				pos.start = first.start;
				pos.file = first.file;
			}
			if(end != null){
				pos.end = end.start;
				if(end.getStartColumn() > 0){
					pos.end -= 1;
				}
				pos.file = end.file;
			}
			return pos;
		}

		public int getStartLine(){
			return Symbol.getLine(start);
		}

		public int getStartColumn(){
			return Symbol.getColumn(start);
		}

		public int getEndLine(){
			return Symbol.getLine(end);
		}

		public int getEndColumn(){
			return Symbol.getColumn(end);
		}

		public String getFileName(){
			return file != null ? file.getName() : null;
		}

		public File getFile(){
			return file;
		}

		public String toString(){
			return file.getName() + " : [" + getStartLine() + ", " + getStartColumn() + "] - [" + getEndLine() + ", " + getEndColumn() + "]";
		}

		private int start, end;
		private File file;
	}
}
