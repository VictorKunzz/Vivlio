const TOTAL = 5;

function Estrelas({ nota }) {
  if (nota == null) {
    return null;
  }

  return (
    <span
      className="estrelas"
      role="img"
      aria-label={`Estado: ${nota} de ${TOTAL} estrelas`}
    >
      {'★'.repeat(nota)}
      <span className="estrelas-vazias">{'★'.repeat(TOTAL - nota)}</span>
    </span>
  );
}

export default Estrelas;
