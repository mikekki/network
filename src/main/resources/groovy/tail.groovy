Object[] info = new Object[2];

info[0] = m.toString();
info[1] = new LinkedList<Object[]>();

for (GroundAtom atom : Queries.getAllAtoms(db, At1)) {
    Object[] record = new Object[3]; 
    record[0] = Integer.parseInt(atom.getArguments()[0].toString().replaceAll("'", ""));
    record[1] = atom.getArguments()[1].toString().replaceAll("'", "");
    record[2] = new Double(atom.getValue());
    info[1].add(record);
}

info;